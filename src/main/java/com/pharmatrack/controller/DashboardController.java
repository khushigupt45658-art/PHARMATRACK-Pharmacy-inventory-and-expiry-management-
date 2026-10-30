package com.pharmatrack.controller;

import com.pharmatrack.dto.ApiResponse;
import com.pharmatrack.dto.DashboardMetricsDto;
import com.pharmatrack.dto.MedicineDto;
import com.pharmatrack.dto.OrderDto;
import com.pharmatrack.model.OrderStatus;
import com.pharmatrack.repository.CustomerOrderRepository;
import com.pharmatrack.repository.MedicineBatchRepository;
import com.pharmatrack.repository.MedicineRepository;
import com.pharmatrack.service.InventoryService;
import com.pharmatrack.service.MedicineService;
import com.pharmatrack.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class DashboardController {

    private final MedicineRepository medicineRepository;
    private final MedicineBatchRepository batchRepository;
    private final CustomerOrderRepository orderRepository;
    private final MedicineService medicineService;
    private final InventoryService inventoryService;
    private final OrderService orderService;

    @Value("${pharmatrack.inventory.near-expiry-days-threshold:30}")
    private int nearExpiryDaysThreshold;

    public DashboardController(MedicineRepository medicineRepository,
                               MedicineBatchRepository batchRepository,
                               CustomerOrderRepository orderRepository,
                               MedicineService medicineService,
                               InventoryService inventoryService,
                               OrderService orderService) {
        this.medicineRepository = medicineRepository;
        this.batchRepository = batchRepository;
        this.orderRepository = orderRepository;
        this.medicineService = medicineService;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<DashboardMetricsDto>> getDashboardMetrics() {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(nearExpiryDaysThreshold);

        DashboardMetricsDto metrics = new DashboardMetricsDto();
        metrics.setTotalActiveMedicines(medicineRepository.countByActiveTrue());
        metrics.setTotalEligibleStockUnits(batchRepository.countTotalEligibleStock(today));
        metrics.setExpiredBatchesCount(batchRepository.countExpiredBatches(today));
        metrics.setNearExpiryBatchesCount(batchRepository.countNearExpiryBatches(today, threshold));
        metrics.setPendingOrdersCount(orderRepository.countByStatus(OrderStatus.PENDING));

        // Get medicines and find low stock items
        List<MedicineDto> allMeds = medicineService.getAllMedicinesForAdmin(null);
        List<MedicineDto> lowStock = allMeds.stream()
                .filter(m -> m.getTotalEligibleStock() <= m.getMinStockThreshold())
                .collect(Collectors.toList());
        metrics.setLowStockCount(lowStock.size());
        metrics.setLowStockMedicines(lowStock.stream().limit(8).collect(Collectors.toList()));

        // Recent Orders
        List<OrderDto> recentOrders = orderService.getAllOrdersForStaff(null).stream()
                .limit(7)
                .collect(Collectors.toList());
        metrics.setRecentOrders(recentOrders);

        // Recent Movements
        metrics.setRecentMovements(inventoryService.getRecentStockMovements().stream().limit(7).collect(Collectors.toList()));

        // Near Expiry Batches preview
        metrics.setNearExpiryBatches(inventoryService.getNearExpiryBatches().stream().limit(8).collect(Collectors.toList()));

        return ResponseEntity.ok(ApiResponse.ok("Dashboard metrics loaded", metrics));
    }
}
