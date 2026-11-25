package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.BranchReviewPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BranchReviewPersistenceRepository extends JpaRepository<BranchReviewPersistenceEntity, UUID> {
    List<BranchReviewPersistenceEntity> findByBranchIdOrderByCreatedAtDesc(UUID branchId);
    boolean existsByAppointmentId(UUID appointmentId);
    int countByBranchId(UUID branchId);

    @Query("SELECT AVG(r.rating) FROM BranchReviewPersistenceEntity r WHERE r.branchId = :branchId AND r.deletedAt IS NULL")
    Double averageRatingByBranchId(@Param("branchId") UUID branchId);
}