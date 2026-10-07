package com.tuxlogic.shiftiq.platform.analytics.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.analytics.infrastructure.persistence.jpa.entities.BranchAnalyticsSnapshotPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaBranchAnalyticsSnapshotRepository extends JpaRepository<BranchAnalyticsSnapshotPersistenceEntity, UUID> {
    Optional<BranchAnalyticsSnapshotPersistenceEntity> findByBranchIdAndSnapshotDate(UUID branchId, LocalDate date);
    List<BranchAnalyticsSnapshotPersistenceEntity> findByBranchIdAndSnapshotDateBetween(UUID branchId, LocalDate startDate, LocalDate endDate);
    List<BranchAnalyticsSnapshotPersistenceEntity> findBySnapshotDate(LocalDate date);
}
