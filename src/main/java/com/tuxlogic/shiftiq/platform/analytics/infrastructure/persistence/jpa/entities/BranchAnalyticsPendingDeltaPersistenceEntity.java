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
@Table(name = "branch_analytics_pending_deltas")
@Getter
@Setter
@NoArgsConstructor
public class BranchAnalyticsPendingDeltaPersistenceEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "delta_date", nullable = false)
    private LocalDate deltaDate;

    @Column(name = "revenue", nullable = false, precision = 12, scale = 2)
    private BigDecimal revenue;

    @Column(name = "work_orders", nullable = false)
    private int workOrders;

    @Column(name = "appointments", nullable = false)
    private int appointments;

    @Column(name = "dtc_alerts", nullable = false)
    private int dtcAlerts;

    @Column(name = "low_stock_recompute", nullable = false)
    private boolean lowStockRecompute;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
