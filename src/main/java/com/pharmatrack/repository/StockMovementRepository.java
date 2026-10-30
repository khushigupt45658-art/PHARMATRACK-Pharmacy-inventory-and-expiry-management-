package com.pharmatrack.repository;

import com.pharmatrack.model.MovementType;
import com.pharmatrack.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findAllByOrderByCreatedAtDesc();

    List<StockMovement> findTop20ByOrderByCreatedAtDesc();

    List<StockMovement> findByMedicineIdOrderByCreatedAtDesc(Long medicineId);

    List<StockMovement> findByMovementTypeOrderByCreatedAtDesc(MovementType movementType);

    List<StockMovement> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
}
