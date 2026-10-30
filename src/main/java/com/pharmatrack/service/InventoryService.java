package com.pharmatrack.service;

import com.pharmatrack.dto.MedicineBatchDto;
import com.pharmatrack.dto.MedicineDto;
import com.pharmatrack.dto.StockAdjustmentDto;
import com.pharmatrack.dto.StockMovementDto;
import com.pharmatrack.exception.BadRequestException;
import com.pharmatrack.exception.InsufficientStockException;
import com.pharmatrack.exception.ResourceNotFoundException;
import com.pharmatrack.model.*;
import com.pharmatrack.repository.MedicineBatchRepository;
import com.pharmatrack.repository.MedicineRepository;
import com.pharmatrack.repository.StockMovementRepository;
import com.pharmatrack.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final MedicineBatchRepository batchRepository;
    private final MedicineRepository medicineRepository;
    private final SupplierRepository supplierRepository;
    private final StockMovementRepository movementRepository;
    private final NotificationService notificationService;

    @Value("${pharmatrack.inventory.near-expiry-days-threshold:30}")
    private int nearExpiryDaysThreshold;

    public InventoryService(MedicineBatchRepository batchRepository,
                            MedicineRepository medicineRepository,
                            SupplierRepository supplierRepository,
                            StockMovementRepository movementRepository,
                            NotificationService notificationService) {
        this.batchRepository = batchRepository;
        this.medicineRepository = medicineRepository;
        this.supplierRepository = supplierRepository;
        this.movementRepository = movementRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<MedicineBatchDto> getAllBatches() {
        LocalDate today = LocalDate.now();
        return batchRepository.findAll().stream()
                .map(batch -> mapBatchToDto(batch, today))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineBatchDto> getBatchesByMedicine(Long medicineId) {
        LocalDate today = LocalDate.now();
        return batchRepository.findByMedicineIdOrderByExpiryDateAsc(medicineId).stream()
                .map(batch -> mapBatchToDto(batch, today))
                .collect(Collectors.toList());
    }

    @Transactional
    public MedicineBatchDto createBatch(MedicineBatchDto dto, String createdBy) {
        Medicine medicine = medicineRepository.findById(dto.getMedicineId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + dto.getMedicineId()));

        if (dto.getExpiryDate().isBefore(dto.getManufacturingDate())) {
            throw new BadRequestException("Expiry date cannot be before manufacturing date");
        }

        if (batchRepository.findByMedicineIdAndBatchNumber(dto.getMedicineId(), dto.getBatchNumber()).isPresent()) {
            throw new BadRequestException("Batch number '" + dto.getBatchNumber() + "' already exists for this medicine");
        }

        Supplier supplier = null;
        if (dto.getSupplierId() != null) {
            supplier = supplierRepository.findById(dto.getSupplierId()).orElse(null);
        }

        MedicineBatch batch = new MedicineBatch();
        batch.setMedicine(medicine);
        batch.setBatchNumber(dto.getBatchNumber().trim().toUpperCase());
        batch.setManufacturingDate(dto.getManufacturingDate());
        batch.setExpiryDate(dto.getExpiryDate());
        batch.setQuantityReceived(dto.getQuantityReceived());
        batch.setAvailableQuantity(dto.getQuantityReceived()); // Initial available = received
        batch.setPurchaseCost(dto.getPurchaseCost());
        batch.setSupplier(supplier);
        batch.setReceiptDate(dto.getReceiptDate() != null ? dto.getReceiptDate() : LocalDate.now());
        updateBatchStatus(batch, LocalDate.now());

        MedicineBatch saved = batchRepository.save(batch);

        // Record stock movement audit
        StockMovement movement = new StockMovement(
                medicine,
                saved,
                MovementType.PURCHASE_RECEIPT,
                saved.getQuantityReceived(),
                saved.getAvailableQuantity(),
                "BATCH-" + saved.getBatchNumber(),
                "Initial batch receipt",
                createdBy != null ? createdBy : "SYSTEM"
        );
        movementRepository.save(movement);

        // Check if this batch is near expiry upon receipt and notify
        if (saved.isNearExpiry(nearExpiryDaysThreshold)) {
            notificationService.createAlert(
                    NotificationType.NEAR_EXPIRY,
                    "Near-Expiry Stock Alert",
                    "Batch " + saved.getBatchNumber() + " of " + medicine.getName() + " expires in " +
                            ChronoUnit.DAYS.between(LocalDate.now(), saved.getExpiryDate()) + " days.",
                    "ROLE_STAFF",
                    null,
                    saved.getId()
            );
        }

        return mapBatchToDto(saved, LocalDate.now());
    }

    /**
     * Stock adjustment with mandatory reason for auditability.
     */
    @Transactional
    public MedicineBatchDto adjustStock(StockAdjustmentDto dto, String userFullName) {
        MedicineBatch batch = batchRepository.findById(dto.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + dto.getBatchId()));

        MovementType movementType;
        int qtyChange = dto.getQuantity();

        if ("INCREASE".equalsIgnoreCase(dto.getAdjustmentType())) {
            batch.setAvailableQuantity(batch.getAvailableQuantity() + qtyChange);
            movementType = MovementType.ADJUSTMENT_INCREASE;
        } else if ("DECREASE".equalsIgnoreCase(dto.getAdjustmentType())) {
            if (batch.getAvailableQuantity() < qtyChange) {
                throw new InsufficientStockException("Cannot decrease " + qtyChange + " units. Only " +
                        batch.getAvailableQuantity() + " units available in batch " + batch.getBatchNumber());
            }
            batch.setAvailableQuantity(batch.getAvailableQuantity() - qtyChange);
            movementType = MovementType.ADJUSTMENT_DECREASE;
        } else if ("DISPOSAL".equalsIgnoreCase(dto.getAdjustmentType())) {
            if (batch.getAvailableQuantity() < qtyChange) {
                throw new InsufficientStockException("Cannot dispose " + qtyChange + " units. Only " +
                        batch.getAvailableQuantity() + " units available in batch " + batch.getBatchNumber());
            }
            batch.setAvailableQuantity(batch.getAvailableQuantity() - qtyChange);
            movementType = MovementType.EXPIRED_DISPOSAL;
        } else {
            throw new BadRequestException("Invalid adjustment type: " + dto.getAdjustmentType());
        }

        updateBatchStatus(batch, LocalDate.now());
        MedicineBatch updated = batchRepository.save(batch);

        // Record stock movement
        StockMovement movement = new StockMovement(
                batch.getMedicine(),
                batch,
                movementType,
                qtyChange,
                updated.getAvailableQuantity(),
                "ADJ-" + System.currentTimeMillis(),
                dto.getReason() + (dto.getNotes() != null ? " (" + dto.getNotes() + ")" : ""),
                userFullName
        );
        movementRepository.save(movement);

        // Check if medicine fell below threshold
        checkLowStockAlert(batch.getMedicine());

        return mapBatchToDto(updated, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<MedicineBatchDto> getNearExpiryBatches() {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(nearExpiryDaysThreshold);
        return batchRepository.findNearExpiryBatches(today, threshold).stream()
                .map(b -> mapBatchToDto(b, today))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicineBatchDto> getExpiredBatches() {
        LocalDate today = LocalDate.now();
        return batchRepository.findExpiredBatchesWithStock(today).stream()
                .map(b -> mapBatchToDto(b, today))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StockMovementDto> getRecentStockMovements() {
        return movementRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(this::mapMovementToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StockMovementDto> getAllStockMovements() {
        return movementRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapMovementToDto)
                .collect(Collectors.toList());
    }

    public void checkLowStockAlert(Medicine medicine) {
        int currentStock = batchRepository.sumEligibleStockByMedicineId(medicine.getId(), LocalDate.now());
        if (currentStock <= medicine.getMinStockThreshold()) {
            notificationService.createAlert(
                    NotificationType.LOW_STOCK,
                    "Low Stock Alert: " + medicine.getName(),
                    "Eligible stock is " + currentStock + " units (threshold: " + medicine.getMinStockThreshold() + "). Restocking required.",
                    "ROLE_STAFF",
                    null,
                    medicine.getId()
            );
        }
    }

    private void updateBatchStatus(MedicineBatch batch, LocalDate today) {
        if (batch.getAvailableQuantity() <= 0) {
            batch.setStatus(BatchStatus.DEPLETED);
        } else if (batch.getExpiryDate().isBefore(today)) {
            batch.setStatus(BatchStatus.EXPIRED);
        } else if (batch.isNearExpiry(nearExpiryDaysThreshold)) {
            batch.setStatus(BatchStatus.NEAR_EXPIRY);
        } else {
            batch.setStatus(BatchStatus.ACTIVE);
        }
    }

    public MedicineBatchDto mapBatchToDto(MedicineBatch batch, LocalDate today) {
        MedicineBatchDto dto = new MedicineBatchDto();
        dto.setId(batch.getId());
        dto.setMedicineId(batch.getMedicine().getId());
        dto.setMedicineName(batch.getMedicine().getName());
        dto.setMedicineSku(batch.getMedicine().getSkuCode());
        dto.setBatchNumber(batch.getBatchNumber());
        dto.setManufacturingDate(batch.getManufacturingDate());
        dto.setExpiryDate(batch.getExpiryDate());
        dto.setQuantityReceived(batch.getQuantityReceived());
        dto.setAvailableQuantity(batch.getAvailableQuantity());
        dto.setPurchaseCost(batch.getPurchaseCost());
        if (batch.getSupplier() != null) {
            dto.setSupplierId(batch.getSupplier().getId());
            dto.setSupplierName(batch.getSupplier().getName());
        }
        dto.setReceiptDate(batch.getReceiptDate());

        boolean isExp = batch.getExpiryDate().isBefore(today);
        boolean isNear = !isExp && batch.isNearExpiry(nearExpiryDaysThreshold);

        dto.setExpired(isExp);
        dto.setNearExpiry(isNear);
        dto.setDaysUntilExpiry(ChronoUnit.DAYS.between(today, batch.getExpiryDate()));

        if (batch.getAvailableQuantity() <= 0) {
            dto.setStatus("DEPLETED");
        } else if (isExp) {
            dto.setStatus("EXPIRED");
        } else if (isNear) {
            dto.setStatus("NEAR_EXPIRY");
        } else {
            dto.setStatus("ACTIVE");
        }

        return dto;
    }

    public StockMovementDto mapMovementToDto(StockMovement movement) {
        StockMovementDto dto = new StockMovementDto();
        dto.setId(movement.getId());
        dto.setMedicineId(movement.getMedicine().getId());
        dto.setMedicineName(movement.getMedicine().getName());
        dto.setSkuCode(movement.getMedicine().getSkuCode());
        dto.setBatchId(movement.getBatch().getId());
        dto.setBatchNumber(movement.getBatch().getBatchNumber());
        dto.setMovementType(movement.getMovementType().name());
        dto.setQuantity(movement.getQuantity());
        dto.setQuantityAfter(movement.getQuantityAfter());
        dto.setReferenceNumber(movement.getReferenceNumber());
        dto.setReason(movement.getReason());
        dto.setCreatedBy(movement.getCreatedBy());
        dto.setCreatedAt(movement.getCreatedAt());
        return dto;
    }
}
