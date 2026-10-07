package com.tuxlogic.shiftiq.platform.analytics.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "branch_analytics_snapshots")
@Getter
@Setter
@NoArgsConstructor
public class BranchAnalyticsSnapshotPersistenceEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "total_revenue", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalRevenue;

    @Column(name = "completed_work_orders_count", nullable = false)
    private Integer completedWorkOrdersCount;

    @Column(name = "total_appointments_count", nullable = false)
    private Integer totalAppointmentsCount;

    @Column(name = "low_stock_alerts_count", nullable = false)
    private Integer lowStockAlertsCount;

    @Column(name = "dtc_alerts_count", nullable = false)
    private Integer dtcAlertsCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
