package com.pharmatrack.dto;

import jakarta.validation.constraints.NotBlank;

public class OrderStatusUpdateRequest {

    @NotBlank(message = "New status is required")
    private String status;

    private String reason;
    private String prescriptionVerifiedBy;

    public OrderStatusUpdateRequest() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getPrescriptionVerifiedBy() { return prescriptionVerifiedBy; }
    public void setPrescriptionVerifiedBy(String prescriptionVerifiedBy) { this.prescriptionVerifiedBy = prescriptionVerifiedBy; }
}
