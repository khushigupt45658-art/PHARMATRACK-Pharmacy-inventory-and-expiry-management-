package com.pharmatrack.controller;

import com.pharmatrack.dto.ApiResponse;
import com.pharmatrack.dto.PurchaseOrderDto;
import com.pharmatrack.dto.SupplierDto;
import com.pharmatrack.model.User;
import com.pharmatrack.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplierDto>>> getAllSuppliers(
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<SupplierDto> suppliers = supplierService.getAllSuppliers(activeOnly);
        return ResponseEntity.ok(ApiResponse.ok("Suppliers retrieved", suppliers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierDto>> getSupplierById(@PathVariable Long id) {
        SupplierDto supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(ApiResponse.ok("Supplier retrieved", supplier));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SupplierDto>> createSupplier(@Valid @RequestBody SupplierDto dto) {
        SupplierDto created = supplierService.createSupplier(dto);
        return ResponseEntity.ok(ApiResponse.ok("Supplier created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SupplierDto>> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody SupplierDto dto) {
        SupplierDto updated = supplierService.updateSupplier(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Supplier updated successfully", updated));
    }

    // Purchase Orders / Restocking
    @GetMapping("/purchase-orders")
    public ResponseEntity<ApiResponse<List<PurchaseOrderDto>>> getAllPurchaseOrders() {
        List<PurchaseOrderDto> orders = supplierService.getAllPurchaseOrders();
        return ResponseEntity.ok(ApiResponse.ok("Purchase orders retrieved", orders));
    }

    @PostMapping("/purchase-orders")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> createPurchaseOrder(@Valid @RequestBody PurchaseOrderDto dto) {
        PurchaseOrderDto created = supplierService.createPurchaseOrder(dto);
        return ResponseEntity.ok(ApiResponse.ok("Purchase order created (Pending receipt)", created));
    }

    @PostMapping("/purchase-orders/{id}/receive")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> receivePurchaseOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        PurchaseOrderDto received = supplierService.receivePurchaseOrder(id, user != null ? user.getFullName() : "STAFF");
        return ResponseEntity.ok(ApiResponse.ok("Purchase order received and stock added to inventory", received));
    }
}
