package com.tuxlogic.shiftiq.platform.analytics.application.internal.dto;

import com.tuxlogic.shiftiq.platform.analytics.domain.model.aggregates.BranchAnalyticsSnapshot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Read model for a single daily analytics snapshot. The application layer returns this
 * record instead of the persistence-backed aggregate so the interface layer never sees
 * domain mutation methods.
 */
public record DailyAnalyticsSnapshot(
        UUID id,
        UUID branchId,
        LocalDate snapshotDate,
        BigDecimal totalRevenue,
        Integer completedWorkOrdersCount,
        Integer totalAppointmentsCount,
        Integer lowStockAlertsCount,
        Integer dtcAlertsCount
) {
    public static DailyAnalyticsSnapshot from(BranchAnalyticsSnapshot snapshot) {
        return new DailyAnalyticsSnapshot(
                snapshot.getId().value(),
                snapshot.getBranchId().value(),
                snapshot.getSnapshotDate(),
                snapshot.getTotalRevenue(),
                snapshot.getCompletedWorkOrdersCount(),
                snapshot.getTotalAppointmentsCount(),
                snapshot.getLowStockAlertsCount(),
                snapshot.getDtcAlertsCount()
        );
    }
}
