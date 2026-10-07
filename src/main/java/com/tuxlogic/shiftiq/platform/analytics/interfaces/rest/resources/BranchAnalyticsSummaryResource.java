package com.tuxlogic.shiftiq.platform.analytics.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.UUID;

public record BranchAnalyticsSummaryResource(
        UUID branchId,
        BigDecimal totalRevenue,
        Integer completedWorkOrdersCount,
        Integer totalAppointmentsCount,
        Integer lowStockAlertsCount,
        Integer dtcAlertsCount
) {}
