package com.tuxlogic.shiftiq.platform.analytics.domain.repositories;

import com.tuxlogic.shiftiq.platform.analytics.domain.model.aggregates.BranchAnalyticsSnapshot;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.valueobjects.AnalyticsDelta;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.valueobjects.SnapshotId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BranchAnalyticsRepository {
    BranchAnalyticsSnapshot save(BranchAnalyticsSnapshot snapshot);
    Optional<BranchAnalyticsSnapshot> findById(SnapshotId id);
    Optional<BranchAnalyticsSnapshot> findByBranchIdAndSnapshotDate(BranchId branchId, LocalDate date);
    List<BranchAnalyticsSnapshot> findByBranchIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(BranchId branchId, LocalDate startDate, LocalDate endDate);
    List<BranchAnalyticsSnapshot> findBySnapshotDate(LocalDate date);
    List<BranchAnalyticsSnapshot> findBySnapshotDateAndBranchIdIn(LocalDate date, Collection<UUID> branchIds);

    /**
     * Atomically creates (when missing) and increments the counters of the snapshot for
     * the given day. The whole change runs in a single SQL statement, so concurrent
     * events never lose an update and the unique (branch, day) constraint cannot race.
     *
     * @return the number of rows touched (always 1 on success)
     */
    int applyDelta(BranchId branchId, LocalDate snapshotDate, AnalyticsDelta delta);

    /**
     * Atomically creates (when missing) and replaces the low stock alert count of the
     * snapshot for the given day with an absolute recomputed value.
     *
     * @return the number of rows touched (always 1 on success)
     */
    int setLowStockAlertsCount(BranchId branchId, LocalDate snapshotDate, int lowStockCount);
}
