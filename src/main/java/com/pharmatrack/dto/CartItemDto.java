package com.pharmatrack.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CartItemDto {

    @NotNull(message = "Medicine ID is required")
    private Long medicineId;

    private String medicineName;
    private BigDecimal unitPrice;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    private boolean prescriptionRequired;

    public CartItemDto() {}

    public CartItemDto(Long medicineId, String medicineName, BigDecimal unitPrice, int quantity, boolean prescriptionRequired) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.prescriptionRequired = prescriptionRequired;
    }

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public boolean isPrescriptionRequired() { return prescriptionRequired; }
    public void setPrescriptionRequired(boolean prescriptionRequired) { this.prescriptionRequired = prescriptionRequired; }
}
