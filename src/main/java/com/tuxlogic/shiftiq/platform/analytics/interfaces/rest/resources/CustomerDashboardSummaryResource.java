package com.tuxlogic.shiftiq.platform.analytics.interfaces.rest.resources;

import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.CustomerDashboardSummary;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CustomerDashboardSummaryResource(
        UUID customerId,
        int activeVehiclesCount,
        int activeDtcAlertsCount,
        int upcomingAppointmentsCount,
        int activeWorkOrdersCount,
        int pendingQuotesCount,
        List<UpcomingAppointmentResource> upcomingAppointments,
        List<ActiveWorkOrderResource> activeWorkOrders,
        List<PendingQuoteResource> pendingQuotes
) {
    public static CustomerDashboardSummaryResource from(CustomerDashboardSummary summary) {
        return new CustomerDashboardSummaryResource(
                summary.customerId(),
                summary.activeVehiclesCount(),
                summary.activeDtcAlertsCount(),
                summary.upcomingAppointmentsCount(),
                summary.activeWorkOrdersCount(),
                summary.pendingQuotesCount(),
                summary.upcomingAppointments().stream()
                        .map(a -> new UpcomingAppointmentResource(
                                a.appointmentId(),
                                a.branchId(),
                                a.vehicleId(),
                                a.scheduledStart(),
                                a.status()
                        ))
                        .toList(),
                summary.activeWorkOrders().stream()
                        .map(wo -> new ActiveWorkOrderResource(
                                wo.workOrderId(),
                                wo.branchId(),
                                wo.vehicleId(),
                                wo.internalNumber(),
                                wo.status()
                        ))
                        .toList(),
                summary.pendingQuotes().stream()
                        .map(q -> new PendingQuoteResource(
                                q.quoteId(),
                                q.workOrderId(),
                                q.branchId(),
                                q.totalAmount(),
                                q.status()
                        ))
                        .toList()
        );
    }

    public record UpcomingAppointmentResource(
            UUID appointmentId,
            UUID branchId,
            UUID vehicleId,
            LocalDateTime scheduledStart,
            String status
    ) {
    }

    public record ActiveWorkOrderResource(
            UUID workOrderId,
            UUID branchId,
            UUID vehicleId,
            Integer internalNumber,
            String status
    ) {
    }

    public record PendingQuoteResource(
            UUID quoteId,
            UUID workOrderId,
            UUID branchId,
            BigDecimal totalAmount,
            String status
    ) {
    }
}
