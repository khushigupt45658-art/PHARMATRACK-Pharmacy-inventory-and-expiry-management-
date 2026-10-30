package com.pharmatrack.repository;

import com.pharmatrack.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    Optional<Medicine> findBySkuCode(String skuCode);

    boolean existsBySkuCode(String skuCode);

    List<Medicine> findByActiveTrueOrderByNameAsc();

    @Query("SELECT m FROM Medicine m WHERE m.active = true " +
           "AND (:categoryId IS NULL OR m.category.id = :categoryId) " +
           "AND (:query IS NULL OR :query = '' OR LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "     OR LOWER(m.genericName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "     OR LOWER(m.manufacturer) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Medicine> searchMedicines(@Param("query") String query, @Param("categoryId") Long categoryId);

    @Query("SELECT m FROM Medicine m WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           " OR LOWER(m.genericName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           " OR LOWER(m.skuCode) LIKE LOWER(CONCAT('%', :query, '%')) " +
           " OR LOWER(m.manufacturer) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Medicine> adminSearchMedicines(@Param("query") String query);

    long countByActiveTrue();
}
