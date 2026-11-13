package com.tuxlogic.shiftiq.platform.analytics.domain.model.queries;

import java.util.List;
import java.util.UUID;

/**
 * Lightweight employee dashboard summary aggregated via read-only ACL ports.
 */
public record EmployeeDashboardSummary(
        UUID employeeId,
        List<UUID> branchIds,
        int assignedWorkOrdersCount,
        int pendingTasksCount,
        int completedTasksCount,
        int activeDtcAlertsCount,
        List<AssignedWorkOrderItem> assignedWorkOrders
) {
    public record AssignedWorkOrderItem(
            UUID workOrderId,
            UUID branchId,
            UUID vehicleId,
            Integer internalNumber,
            String status,
            int assignedTasksCount,
            int pendingTasksCount
    ) {
    }
}
