package com.pharmatrack.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class MedicineDto {
    private Long id;

    @NotBlank(message = "Medicine name is required")
    private String name;

    private String genericName;

    @NotBlank(message = "SKU/Code is required")
    private String skuCode;

    @NotNull(message = "Category is required")
    private Long categoryId;
    private String categoryName;

    @NotBlank(message = "Manufacturer is required")
    private String manufacturer;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal unitPrice;

    @Min(value = 0, message = "Threshold cannot be negative")
    private int minStockThreshold = 20;

    private boolean prescriptionRequired;
    private String description;
    private String dosageForm;
    private String imageUrl;
    private boolean active = true;

    // Computed inventory fields
    private int totalEligibleStock;
    private boolean available;
    private String stockStatus; // IN_STOCK, LOW_STOCK, OUT_OF_STOCK
    private LocalDate earliestExpiryDate;

    public MedicineDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }

    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public int getMinStockThreshold() { return minStockThreshold; }
    public void setMinStockThreshold(int minStockThreshold) { this.minStockThreshold = minStockThreshold; }

    public boolean isPrescriptionRequired() { return prescriptionRequired; }
    public void setPrescriptionRequired(boolean prescriptionRequired) { this.prescriptionRequired = prescriptionRequired; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDosageForm() { return dosageForm; }
    public void setDosageForm(String dosageForm) { this.dosageForm = dosageForm; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getTotalEligibleStock() { return totalEligibleStock; }
    public void setTotalEligibleStock(int totalEligibleStock) { this.totalEligibleStock = totalEligibleStock; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }

    public LocalDate getEarliestExpiryDate() { return earliestExpiryDate; }
    public void setEarliestExpiryDate(LocalDate earliestExpiryDate) { this.earliestExpiryDate = earliestExpiryDate; }
}
