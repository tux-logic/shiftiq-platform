package com.tuxlogic.shiftiq.platform.analytics.domain.repositories;

import com.tuxlogic.shiftiq.platform.analytics.domain.model.aggregates.BranchAnalyticsSnapshot;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.valueobjects.SnapshotId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BranchAnalyticsRepository {
    BranchAnalyticsSnapshot save(BranchAnalyticsSnapshot snapshot);
    Optional<BranchAnalyticsSnapshot> findById(SnapshotId id);
    Optional<BranchAnalyticsSnapshot> findByBranchIdAndSnapshotDate(BranchId branchId, LocalDate date);
    List<BranchAnalyticsSnapshot> findByBranchIdAndSnapshotDateBetween(BranchId branchId, LocalDate startDate, LocalDate endDate);
    List<BranchAnalyticsSnapshot> findBySnapshotDate(LocalDate date);
}
