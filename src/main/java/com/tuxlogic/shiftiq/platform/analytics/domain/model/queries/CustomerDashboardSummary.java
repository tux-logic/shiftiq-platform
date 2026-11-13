package com.tuxlogic.shiftiq.platform.analytics.domain.model.queries;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Lightweight customer dashboard summary aggregated via read-only ACL ports.
 */
public record CustomerDashboardSummary(
        UUID customerId,
        int activeVehiclesCount,
        int activeDtcAlertsCount,
        int upcomingAppointmentsCount,
        int activeWorkOrdersCount,
        int pendingQuotesCount,
        List<UpcomingAppointmentItem> upcomingAppointments,
        List<ActiveWorkOrderItem> activeWorkOrders,
        List<PendingQuoteItem> pendingQuotes
) {
    public record UpcomingAppointmentItem(
            UUID appointmentId,
            UUID branchId,
            UUID vehicleId,
            LocalDateTime scheduledStart,
            String status
    ) {
    }

    public record ActiveWorkOrderItem(
            UUID workOrderId,
            UUID branchId,
            UUID vehicleId,
            Integer internalNumber,
            String status
    ) {
    }

    public record PendingQuoteItem(
            UUID quoteId,
            UUID workOrderId,
            UUID branchId,
            BigDecimal totalAmount,
            String status
    ) {
    }
}
