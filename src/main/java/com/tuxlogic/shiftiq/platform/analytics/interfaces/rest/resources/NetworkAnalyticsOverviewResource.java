package com.tuxlogic.shiftiq.platform.analytics.interfaces.rest.resources;

import java.math.BigDecimal;

public record NetworkAnalyticsOverviewResource(
        Integer totalActiveBranchesCount,
        BigDecimal totalNetworkRevenue,
        Integer totalNetworkCompletedWorkOrders,
        Integer totalNetworkAppointments,
        Integer totalNetworkDtcAlerts
) {}
