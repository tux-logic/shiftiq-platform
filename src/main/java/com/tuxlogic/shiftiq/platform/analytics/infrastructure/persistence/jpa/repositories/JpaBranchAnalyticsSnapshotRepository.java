package com.tuxlogic.shiftiq.platform.analytics.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.analytics.infrastructure.persistence.jpa.entities.BranchAnalyticsSnapshotPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaBranchAnalyticsSnapshotRepository extends JpaRepository<BranchAnalyticsSnapshotPersistenceEntity, UUID> {
    Optional<BranchAnalyticsSnapshotPersistenceEntity> findByBranchIdAndSnapshotDate(UUID branchId, LocalDate date);
    List<BranchAnalyticsSnapshotPersistenceEntity> findByBranchIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(UUID branchId, LocalDate startDate, LocalDate endDate);
    List<BranchAnalyticsSnapshotPersistenceEntity> findBySnapshotDate(LocalDate date);
    List<BranchAnalyticsSnapshotPersistenceEntity> findBySnapshotDateAndBranchIdIn(LocalDate date, Collection<UUID> branchIds);

    /**
     * Atomic upsert: inserts a zeroed snapshot when the (branch, day) row does not exist,
     * otherwise adds the delta to the existing row. Counters and revenue are clamped at
     * zero so a corrective (negative) delta never produces negative KPIs.
     */
    @Modifying(clearAutomatically = true)
    @Query(value = """
            INSERT INTO branch_analytics_snapshots
                (id, branch_id, snapshot_date, total_revenue, completed_work_orders_count,
                 total_appointments_count, low_stock_alerts_count, dtc_alerts_count,
                 created_at, updated_at)
            VALUES (:id, :branchId, :snapshotDate, :revenue, :workOrders, :appointments,
                    0, :dtcAlerts, :updatedAt, :updatedAt)
            ON CONFLICT (branch_id, snapshot_date) DO UPDATE SET
                total_revenue = GREATEST(branch_analytics_snapshots.total_revenue + EXCLUDED.total_revenue, 0),
                completed_work_orders_count = GREATEST(branch_analytics_snapshots.completed_work_orders_count + EXCLUDED.completed_work_orders_count, 0),
                total_appointments_count = GREATEST(branch_analytics_snapshots.total_appointments_count + EXCLUDED.total_appointments_count, 0),
                dtc_alerts_count = GREATEST(branch_analytics_snapshots.dtc_alerts_count + EXCLUDED.dtc_alerts_count, 0),
                updated_at = EXCLUDED.updated_at
            """, nativeQuery = true)
    int applyDelta(@Param("id") UUID id,
                   @Param("branchId") UUID branchId,
                   @Param("snapshotDate") LocalDate snapshotDate,
                   @Param("revenue") BigDecimal revenue,
                   @Param("workOrders") int workOrders,
                   @Param("appointments") int appointments,
                   @Param("dtcAlerts") int dtcAlerts,
                   @Param("updatedAt") Instant updatedAt);

    /**
     * Atomic upsert that stores an absolute (recomputed) low stock alert count.
     */
    @Modifying(clearAutomatically = true)
    @Query(value = """
            INSERT INTO branch_analytics_snapshots
                (id, branch_id, snapshot_date, total_revenue, completed_work_orders_count,
                 total_appointments_count, low_stock_alerts_count, dtc_alerts_count,
                 created_at, updated_at)
            VALUES (:id, :branchId, :snapshotDate, 0, 0, 0, :lowStockCount, 0, :updatedAt, :updatedAt)
            ON CONFLICT (branch_id, snapshot_date) DO UPDATE SET
                low_stock_alerts_count = GREATEST(EXCLUDED.low_stock_alerts_count, 0),
                updated_at = EXCLUDED.updated_at
            """, nativeQuery = true)
    int upsertLowStockAlertsCount(@Param("id") UUID id,
                                  @Param("branchId") UUID branchId,
                                  @Param("snapshotDate") LocalDate snapshotDate,
                                  @Param("lowStockCount") int lowStockCount,
                                  @Param("updatedAt") Instant updatedAt);
}
