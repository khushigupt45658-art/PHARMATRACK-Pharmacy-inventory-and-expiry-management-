package com.pharmatrack.repository;

import com.pharmatrack.model.BatchStatus;
import com.pharmatrack.model.MedicineBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineBatchRepository extends JpaRepository<MedicineBatch, Long> {

    List<MedicineBatch> findByMedicineIdOrderByExpiryDateAsc(Long medicineId);

    Optional<MedicineBatch> findByMedicineIdAndBatchNumber(Long medicineId, String batchNumber);

    /**
     * FEFO Allocation Query:
     * Finds active, non-expired batches with positive available stock for a medicine,
     * sorted strictly in First-Expired, First-Out order.
     */
    @Query("SELECT b FROM MedicineBatch b WHERE b.medicine.id = :medicineId " +
           "AND b.availableQuantity > 0 " +
           "AND b.expiryDate > :currentDate " +
           "ORDER BY b.expiryDate ASC")
    List<MedicineBatch> findEligibleBatchesForOrder(@Param("medicineId") Long medicineId,
                                                   @Param("currentDate") LocalDate currentDate);

    /**
     * Calculate total eligible stock available for sale (non-expired)
     */
    @Query("SELECT COALESCE(SUM(b.availableQuantity), 0) FROM MedicineBatch b " +
           "WHERE b.medicine.id = :medicineId " +
           "AND b.expiryDate > :currentDate")
    int sumEligibleStockByMedicineId(@Param("medicineId") Long medicineId,
                                    @Param("currentDate") LocalDate currentDate);

    /**
     * Batches expiring soon (within threshold days) and still holding stock
     */
    @Query("SELECT b FROM MedicineBatch b WHERE b.availableQuantity > 0 " +
           "AND b.expiryDate > :currentDate AND b.expiryDate <= :thresholdDate " +
           "ORDER BY b.expiryDate ASC")
    List<MedicineBatch> findNearExpiryBatches(@Param("currentDate") LocalDate currentDate,
                                             @Param("thresholdDate") LocalDate thresholdDate);

    /**
     * Batches that have already expired and still have remaining quantity
     */
    @Query("SELECT b FROM MedicineBatch b WHERE b.expiryDate <= :currentDate " +
           "AND b.availableQuantity > 0 " +
           "ORDER BY b.expiryDate ASC")
    List<MedicineBatch> findExpiredBatchesWithStock(@Param("currentDate") LocalDate currentDate);

    List<MedicineBatch> findByStatus(BatchStatus status);

    @Query("SELECT COALESCE(SUM(b.availableQuantity), 0) FROM MedicineBatch b WHERE b.expiryDate > :currentDate")
    long countTotalEligibleStock(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT COUNT(b) FROM MedicineBatch b WHERE b.availableQuantity > 0 AND b.expiryDate <= :currentDate")
    long countExpiredBatches(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT COUNT(b) FROM MedicineBatch b WHERE b.availableQuantity > 0 " +
           "AND b.expiryDate > :currentDate AND b.expiryDate <= :thresholdDate")
    long countNearExpiryBatches(@Param("currentDate") LocalDate currentDate,
                               @Param("thresholdDate") LocalDate thresholdDate);
}
