package com.pharmatrack.dto;

import java.util.List;

public class DashboardMetricsDto {
    private long totalActiveMedicines;
    private long totalEligibleStockUnits;
    private long lowStockCount;
    private long nearExpiryBatchesCount;
    private long expiredBatchesCount;
    private long pendingOrdersCount;

    private List<OrderDto> recentOrders;
    private List<StockMovementDto> recentMovements;
    private List<MedicineDto> lowStockMedicines;
    private List<MedicineBatchDto> nearExpiryBatches;

    public DashboardMetricsDto() {}

    public long getTotalActiveMedicines() { return totalActiveMedicines; }
    public void setTotalActiveMedicines(long totalActiveMedicines) { this.totalActiveMedicines = totalActiveMedicines; }

    public long getTotalEligibleStockUnits() { return totalEligibleStockUnits; }
    public void setTotalEligibleStockUnits(long totalEligibleStockUnits) { this.totalEligibleStockUnits = totalEligibleStockUnits; }

    public long getLowStockCount() { return lowStockCount; }
    public void setLowStockCount(long lowStockCount) { this.lowStockCount = lowStockCount; }

    public long getNearExpiryBatchesCount() { return nearExpiryBatchesCount; }
    public void setNearExpiryBatchesCount(long nearExpiryBatchesCount) { this.nearExpiryBatchesCount = nearExpiryBatchesCount; }

    public long getExpiredBatchesCount() { return expiredBatchesCount; }
    public void setExpiredBatchesCount(long expiredBatchesCount) { this.expiredBatchesCount = expiredBatchesCount; }

    public long getPendingOrdersCount() { return pendingOrdersCount; }
    public void setPendingOrdersCount(long pendingOrdersCount) { this.pendingOrdersCount = pendingOrdersCount; }

    public List<OrderDto> getRecentOrders() { return recentOrders; }
    public void setRecentOrders(List<OrderDto> recentOrders) { this.recentOrders = recentOrders; }

    public List<StockMovementDto> getRecentMovements() { return recentMovements; }
    public void setRecentMovements(List<StockMovementDto> recentMovements) { this.recentMovements = recentMovements; }

    public List<MedicineDto> getLowStockMedicines() { return lowStockMedicines; }
    public void setLowStockMedicines(List<MedicineDto> lowStockMedicines) { this.lowStockMedicines = lowStockMedicines; }

    public List<MedicineBatchDto> getNearExpiryBatches() { return nearExpiryBatches; }
    public void setNearExpiryBatches(List<MedicineBatchDto> nearExpiryBatches) { this.nearExpiryBatches = nearExpiryBatches; }
}
