package com.pharmatrack.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StockAdjustmentDto {

    @NotNull(message = "Batch ID is required")
    private Long batchId;

    @NotBlank(message = "Adjustment type is required (INCREASE, DECREASE, DISPOSAL)")
    private String adjustmentType;

    @Min(value = 1, message = "Adjustment quantity must be at least 1")
    private int quantity;

    @NotBlank(message = "Adjustment reason is mandatory for audit trail")
    private String reason;

    private String notes;

    public StockAdjustmentDto() {}

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getAdjustmentType() { return adjustmentType; }
    public void setAdjustmentType(String adjustmentType) { this.adjustmentType = adjustmentType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
