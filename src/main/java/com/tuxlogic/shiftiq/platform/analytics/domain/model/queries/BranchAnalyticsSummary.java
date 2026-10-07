package com.tuxlogic.shiftiq.platform.analytics.domain.model.queries;

import java.math.BigDecimal;
import java.util.UUID;

public record BranchAnalyticsSummary(
        UUID branchId,
        BigDecimal totalRevenue,
        Integer completedWorkOrdersCount,
        Integer totalAppointmentsCount,
        Integer lowStockAlertsCount,
        Integer dtcAlertsCount
) {}
