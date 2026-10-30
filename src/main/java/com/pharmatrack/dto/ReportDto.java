package com.pharmatrack.dto;

import java.math.BigDecimal;
import java.util.List;

public class ReportDto {
    private String reportType;
    private String generatedAt;
    private int totalRecords;
    private BigDecimal totalValue;
    private List<?> records;

    public ReportDto() {}

    public ReportDto(String reportType, String generatedAt, int totalRecords, BigDecimal totalValue, List<?> records) {
        this.reportType = reportType;
        this.generatedAt = generatedAt;
        this.totalRecords = totalRecords;
        this.totalValue = totalValue;
        this.records = records;
    }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }

    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }

    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }

    public List<?> getRecords() { return records; }
    public void setRecords(List<?> records) { this.records = records; }
}
