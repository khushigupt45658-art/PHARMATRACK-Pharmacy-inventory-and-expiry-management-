package com.pharmatrack.controller;

import com.pharmatrack.dto.ApiResponse;
import com.pharmatrack.dto.ReportDto;
import com.pharmatrack.model.OrderStatus;
import com.pharmatrack.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<ReportDto>> getInventoryReport() {
        ReportDto report = reportService.getInventoryReport();
        return ResponseEntity.ok(ApiResponse.ok("Inventory report generated", report));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<ReportDto>> getLowStockReport() {
        ReportDto report = reportService.getLowStockReport();
        return ResponseEntity.ok(ApiResponse.ok("Low stock report generated", report));
    }

    @GetMapping("/near-expiry")
    public ResponseEntity<ApiResponse<ReportDto>> getNearExpiryReport() {
        ReportDto report = reportService.getNearExpiryReport();
        return ResponseEntity.ok(ApiResponse.ok("Near expiry report generated", report));
    }

    @GetMapping("/expired")
    public ResponseEntity<ApiResponse<ReportDto>> getExpiredReport() {
        ReportDto report = reportService.getExpiredStockReport();
        return ResponseEntity.ok(ApiResponse.ok("Expired stock report generated", report));
    }

    @GetMapping("/movements")
    public ResponseEntity<ApiResponse<ReportDto>> getMovementsReport() {
        ReportDto report = reportService.getStockMovementReport();
        return ResponseEntity.ok(ApiResponse.ok("Stock movements report generated", report));
    }

    @GetMapping("/sales")
    public ResponseEntity<ApiResponse<ReportDto>> getSalesReport(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        OrderStatus orderStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                orderStatus = OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        ReportDto report = reportService.getSalesReport(orderStatus, start, end);
        return ResponseEntity.ok(ApiResponse.ok("Sales report generated", report));
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(@RequestParam String type) {
        byte[] csvData = reportService.generateCsvReport(type);
        String filename = "pharmatrack-" + type.toLowerCase() + "-report.csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
