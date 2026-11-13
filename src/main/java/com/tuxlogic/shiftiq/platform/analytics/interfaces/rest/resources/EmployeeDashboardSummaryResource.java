package com.tuxlogic.shiftiq.platform.analytics.interfaces.rest.resources;

import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.EmployeeDashboardSummary;

import java.util.List;
import java.util.UUID;

public record EmployeeDashboardSummaryResource(
        UUID employeeId,
        List<UUID> branchIds,
        int assignedWorkOrdersCount,
        int pendingTasksCount,
        int completedTasksCount,
        int activeDtcAlertsCount,
        List<AssignedWorkOrderResource> assignedWorkOrders
) {
    public static EmployeeDashboardSummaryResource from(EmployeeDashboardSummary summary) {
        return new EmployeeDashboardSummaryResource(
                summary.employeeId(),
                summary.branchIds(),
                summary.assignedWorkOrdersCount(),
                summary.pendingTasksCount(),
                summary.completedTasksCount(),
                summary.activeDtcAlertsCount(),
                summary.assignedWorkOrders().stream()
                        .map(wo -> new AssignedWorkOrderResource(
                                wo.workOrderId(),
                                wo.branchId(),
                                wo.vehicleId(),
                                wo.internalNumber(),
                                wo.status(),
                                wo.assignedTasksCount(),
                                wo.pendingTasksCount()
                        ))
                        .toList()
        );
    }

    public record AssignedWorkOrderResource(
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
