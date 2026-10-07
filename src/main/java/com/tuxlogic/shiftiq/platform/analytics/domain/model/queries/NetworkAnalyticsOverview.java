package com.tuxlogic.shiftiq.platform.analytics.domain.model.queries;

import java.math.BigDecimal;

public record NetworkAnalyticsOverview(
        Integer totalActiveBranchesCount,
        BigDecimal totalNetworkRevenue,
        Integer totalNetworkCompletedWorkOrders,
        Integer totalNetworkAppointments,
        Integer totalNetworkDtcAlerts
) {}
