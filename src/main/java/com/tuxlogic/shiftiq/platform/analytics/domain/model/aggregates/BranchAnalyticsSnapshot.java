package com.tuxlogic.shiftiq.platform.analytics.domain.model.aggregates;

import com.tuxlogic.shiftiq.platform.analytics.domain.model.valueobjects.SnapshotId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
public class BranchAnalyticsSnapshot extends AbstractDomainAggregateRoot<BranchAnalyticsSnapshot> {

    private SnapshotId id;
    private BranchId branchId;
    private LocalDate snapshotDate;
    private BigDecimal totalRevenue;
    private Integer completedWorkOrdersCount;
    private Integer totalAppointmentsCount;
    private Integer lowStockAlertsCount;
    private Integer dtcAlertsCount;
    private Instant createdAt;
    private Instant updatedAt;

    protected BranchAnalyticsSnapshot() {
    }

    public BranchAnalyticsSnapshot(BranchId branchId, LocalDate snapshotDate) {
        if (branchId == null) {
            throw new IllegalArgumentException("analytics.error.branchId.required");
        }
        if (snapshotDate == null) {
            throw new IllegalArgumentException("analytics.error.snapshotDate.required");
        }
        this.id = new SnapshotId(UUID.randomUUID());
        this.branchId = branchId;
        this.snapshotDate = snapshotDate;
        this.totalRevenue = BigDecimal.ZERO;
        this.completedWorkOrdersCount = 0;
        this.totalAppointmentsCount = 0;
        this.lowStockAlertsCount = 0;
        this.dtcAlertsCount = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public BranchAnalyticsSnapshot(SnapshotId id, BranchId branchId, LocalDate snapshotDate,
                                   BigDecimal totalRevenue, Integer completedWorkOrdersCount,
                                   Integer totalAppointmentsCount, Integer lowStockAlertsCount,
                                   Integer dtcAlertsCount, Instant createdAt, Instant updatedAt) {
        if (id == null) {
            throw new IllegalArgumentException("analytics.error.snapshotId.required");
        }
        if (branchId == null) {
            throw new IllegalArgumentException("analytics.error.branchId.required");
        }
        if (snapshotDate == null) {
            throw new IllegalArgumentException("analytics.error.snapshotDate.required");
        }
        if (createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("analytics.error.snapshotTimestamps.required");
        }
        this.id = id;
        this.branchId = branchId;
        this.snapshotDate = snapshotDate;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.completedWorkOrdersCount = completedWorkOrdersCount != null ? completedWorkOrdersCount : 0;
        this.totalAppointmentsCount = totalAppointmentsCount != null ? totalAppointmentsCount : 0;
        this.lowStockAlertsCount = lowStockAlertsCount != null ? lowStockAlertsCount : 0;
        this.dtcAlertsCount = dtcAlertsCount != null ? dtcAlertsCount : 0;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    public void addRevenue(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.totalRevenue = this.totalRevenue.add(amount);
            this.updatedAt = Instant.now();
        }
    }

    public void incrementCompletedWorkOrders() {
        this.completedWorkOrdersCount++;
        this.updatedAt = Instant.now();
    }

    public void incrementTotalAppointments() {
        this.totalAppointmentsCount++;
        this.updatedAt = Instant.now();
    }

    public void updateLowStockAlertsCount(int count) {
        this.lowStockAlertsCount = Math.max(0, count);
        this.updatedAt = Instant.now();
    }

    public void incrementDtcAlertsCount() {
        this.dtcAlertsCount++;
        this.updatedAt = Instant.now();
    }
}
