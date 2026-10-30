package com.pharmatrack.controller;

import com.pharmatrack.dto.ApiResponse;
import com.pharmatrack.dto.CategoryDto;
import com.pharmatrack.dto.MedicineDto;
import com.pharmatrack.service.MedicineService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    // Public Medicine APIs
    @GetMapping("/medicines")
    public ResponseEntity<ApiResponse<List<MedicineDto>>> searchMedicines(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean inStockOnly) {
        List<MedicineDto> list = medicineService.searchPublicMedicines(query, categoryId, inStockOnly);
        return ResponseEntity.ok(ApiResponse.ok("Medicines retrieved", list));
    }

    @GetMapping("/medicines/{id}")
    public ResponseEntity<ApiResponse<MedicineDto>> getMedicineDetails(@PathVariable Long id) {
        MedicineDto dto = medicineService.getMedicineDetails(id);
        return ResponseEntity.ok(ApiResponse.ok("Medicine details retrieved", dto));
    }

    // Admin / Staff Medicine Management
    @GetMapping("/medicines/admin/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<MedicineDto>>> getAllMedicinesForAdmin(
            @RequestParam(required = false) String query) {
        List<MedicineDto> list = medicineService.getAllMedicinesForAdmin(query);
        return ResponseEntity.ok(ApiResponse.ok("All medicines retrieved", list));
    }

    @PostMapping("/medicines")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MedicineDto>> createMedicine(@Valid @RequestBody MedicineDto dto) {
        MedicineDto created = medicineService.createMedicine(dto);
        return ResponseEntity.ok(ApiResponse.ok("Medicine created successfully", created));
    }

    @PutMapping("/medicines/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MedicineDto>> updateMedicine(
            @PathVariable Long id,
            @Valid @RequestBody MedicineDto dto) {
        MedicineDto updated = medicineService.updateMedicine(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Medicine updated successfully", updated));
    }

    @PatchMapping("/medicines/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleStatus(@PathVariable Long id) {
        medicineService.toggleMedicineStatus(id);
        return ResponseEntity.ok(ApiResponse.ok("Medicine status toggled"));
    }

    // Category Endpoints
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getActiveCategories() {
        List<CategoryDto> categories = medicineService.getActiveCategories();
        return ResponseEntity.ok(ApiResponse.ok("Categories retrieved", categories));
    }

    @GetMapping("/categories/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllCategories() {
        List<CategoryDto> categories = medicineService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.ok("All categories retrieved", categories));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(@Valid @RequestBody CategoryDto dto) {
        CategoryDto created = medicineService.createCategory(dto);
        return ResponseEntity.ok(ApiResponse.ok("Category created successfully", created));
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDto dto) {
        CategoryDto updated = medicineService.updateCategory(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Category updated successfully", updated));
    }
}
