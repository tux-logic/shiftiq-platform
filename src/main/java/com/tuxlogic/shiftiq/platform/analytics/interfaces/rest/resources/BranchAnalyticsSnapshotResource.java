package com.tuxlogic.shiftiq.platform.analytics.interfaces.rest.resources;

import com.tuxlogic.shiftiq.platform.analytics.application.internal.dto.DailyAnalyticsSnapshot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BranchAnalyticsSnapshotResource(
        UUID id,
        UUID branchId,
        LocalDate snapshotDate,
        BigDecimal totalRevenue,
        Integer completedWorkOrdersCount,
        Integer totalAppointmentsCount,
        Integer lowStockAlertsCount,
        Integer dtcAlertsCount
) {
    public static BranchAnalyticsSnapshotResource from(DailyAnalyticsSnapshot snapshot) {
        return new BranchAnalyticsSnapshotResource(
                snapshot.id(),
                snapshot.branchId(),
                snapshot.snapshotDate(),
                snapshot.totalRevenue(),
                snapshot.completedWorkOrdersCount(),
                snapshot.totalAppointmentsCount(),
                snapshot.lowStockAlertsCount(),
                snapshot.dtcAlertsCount()
        );
    }
}
