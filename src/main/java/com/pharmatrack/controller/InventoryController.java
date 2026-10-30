package com.pharmatrack.controller;

import com.pharmatrack.dto.ApiResponse;
import com.pharmatrack.dto.MedicineBatchDto;
import com.pharmatrack.dto.StockAdjustmentDto;
import com.pharmatrack.dto.StockMovementDto;
import com.pharmatrack.model.User;
import com.pharmatrack.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/batches")
    public ResponseEntity<ApiResponse<List<MedicineBatchDto>>> getAllBatches() {
        List<MedicineBatchDto> batches = inventoryService.getAllBatches();
        return ResponseEntity.ok(ApiResponse.ok("Batches retrieved", batches));
    }

    @GetMapping("/batches/medicine/{medicineId}")
    public ResponseEntity<ApiResponse<List<MedicineBatchDto>>> getBatchesByMedicine(@PathVariable Long medicineId) {
        List<MedicineBatchDto> batches = inventoryService.getBatchesByMedicine(medicineId);
        return ResponseEntity.ok(ApiResponse.ok("Batches retrieved for medicine", batches));
    }

    @PostMapping("/batches")
    public ResponseEntity<ApiResponse<MedicineBatchDto>> createBatch(
            @Valid @RequestBody MedicineBatchDto dto,
            @AuthenticationPrincipal User user) {
        MedicineBatchDto created = inventoryService.createBatch(dto, user != null ? user.getFullName() : "STAFF");
        return ResponseEntity.ok(ApiResponse.ok("Batch created and stock added", created));
    }

    @PostMapping("/adjustments")
    public ResponseEntity<ApiResponse<MedicineBatchDto>> adjustStock(
            @Valid @RequestBody StockAdjustmentDto dto,
            @AuthenticationPrincipal User user) {
        MedicineBatchDto adjusted = inventoryService.adjustStock(dto, user != null ? user.getFullName() : "STAFF");
        return ResponseEntity.ok(ApiResponse.ok("Stock adjusted and movement recorded", adjusted));
    }

    @GetMapping("/alerts/near-expiry")
    public ResponseEntity<ApiResponse<List<MedicineBatchDto>>> getNearExpiryAlerts() {
        List<MedicineBatchDto> alerts = inventoryService.getNearExpiryBatches();
        return ResponseEntity.ok(ApiResponse.ok("Near expiry batches retrieved", alerts));
    }

    @GetMapping("/alerts/expired")
    public ResponseEntity<ApiResponse<List<MedicineBatchDto>>> getExpiredAlerts() {
        List<MedicineBatchDto> alerts = inventoryService.getExpiredBatches();
        return ResponseEntity.ok(ApiResponse.ok("Expired batches retrieved", alerts));
    }

    @GetMapping("/movements")
    public ResponseEntity<ApiResponse<List<StockMovementDto>>> getStockMovements() {
        List<StockMovementDto> movements = inventoryService.getAllStockMovements();
        return ResponseEntity.ok(ApiResponse.ok("Stock movement history retrieved", movements));
    }
}
