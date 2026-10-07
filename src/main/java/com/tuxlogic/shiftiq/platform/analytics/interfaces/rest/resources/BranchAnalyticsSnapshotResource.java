package com.tuxlogic.shiftiq.platform.analytics.interfaces.rest.resources;

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
) {}
