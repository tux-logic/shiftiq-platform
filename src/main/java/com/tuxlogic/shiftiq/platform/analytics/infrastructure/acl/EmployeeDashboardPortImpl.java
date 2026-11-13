package com.tuxlogic.shiftiq.platform.analytics.infrastructure.acl;

import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.EmployeeDashboardSummary;
import com.tuxlogic.shiftiq.platform.analytics.domain.model.queries.EmployeeDashboardSummary.AssignedWorkOrderItem;
import com.tuxlogic.shiftiq.platform.analytics.domain.services.EmployeeDashboardPort;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.EmployeeQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetEmployeeByUserIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.UserId;
import com.tuxlogic.shiftiq.platform.fleet.application.queryservices.EmployeeRegistrationQueryService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationByEmployeeIdQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.EmployeeRegistrationStatus;
import com.tuxlogic.shiftiq.platform.iot.application.queryservices.DtcAlertQueryService;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetVehicleDtcAlertHistoryQuery;
import com.tuxlogic.shiftiq.platform.operations.application.queryservices.WorkOrderQueryService;
import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.WorkOrder;
import com.tuxlogic.shiftiq.platform.operations.domain.model.entities.WorkOrderTask;
import com.tuxlogic.shiftiq.platform.operations.domain.model.queries.GetWorkOrdersByBranchIdQuery;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderStatus;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderTaskStatus;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Read-only anti-corruption adapter implementing {@link EmployeeDashboardPort}
 * exclusively through published {@code QueryService} contracts.
 */
@Component
public class EmployeeDashboardPortImpl implements EmployeeDashboardPort {

    private static final int PREVIEW_LIMIT = 10;

    private final EmployeeQueryService employeeQueryService;
    private final EmployeeRegistrationQueryService employeeRegistrationQueryService;
    private final WorkOrderQueryService workOrderQueryService;
    private final DtcAlertQueryService dtcAlertQueryService;

    public EmployeeDashboardPortImpl(
            EmployeeQueryService employeeQueryService,
            EmployeeRegistrationQueryService employeeRegistrationQueryService,
            WorkOrderQueryService workOrderQueryService,
            DtcAlertQueryService dtcAlertQueryService
    ) {
        this.employeeQueryService = employeeQueryService;
        this.employeeRegistrationQueryService = employeeRegistrationQueryService;
        this.workOrderQueryService = workOrderQueryService;
        this.dtcAlertQueryService = dtcAlertQueryService;
    }

    @Override
    public Optional<EmployeeDashboardSummary> loadSummaryForUser(UUID userId) {
        if (userId == null) {
            return Optional.empty();
        }
        var employeeOpt = employeeQueryService.handle(new GetEmployeeByUserIdQuery(new UserId(userId)));
        if (employeeOpt.isEmpty()) {
            return Optional.empty();
        }

        UUID employeeUuid = employeeOpt.get().getId().value();
        var registrationOpt = employeeRegistrationQueryService
                .handle(new GetEmployeeRegistrationByEmployeeIdQuery(employeeUuid))
                .success()
                .filter(reg -> reg.getDeletedAt() == null && reg.getStatus() == EmployeeRegistrationStatus.ACTIVE);

        List<UUID> branchIds = registrationOpt
                .map(reg -> List.of(reg.getBranchId().value()))
                .orElseGet(List::of);

        int pendingTasksCount = 0;
        int completedTasksCount = 0;
        List<AssignedWorkOrderItem> assignedItems = new ArrayList<>();
        List<UUID> activeVehicleIds = new ArrayList<>();

        for (UUID branchId : branchIds) {
            List<WorkOrder> branchOrders = workOrderQueryService.handle(new GetWorkOrdersByBranchIdQuery(new BranchId(branchId)));
            for (WorkOrder wo : branchOrders) {
                if (wo.getDeletedAt() != null) {
                    continue;
                }
                List<WorkOrderTask> myTasks = wo.getTasks() == null ? List.of() : wo.getTasks().stream()
                        .filter(t -> t.getDeletedAt() == null
                                && t.getAssignedMechanicId() != null
                                && employeeUuid.equals(t.getAssignedMechanicId().value()))
                        .toList();

                if (myTasks.isEmpty()) {
                    continue;
                }

                int orderPending = 0;
                for (WorkOrderTask task : myTasks) {
                    if (task.getStatus() == WorkOrderTaskStatus.COMPLETED) {
                        completedTasksCount++;
                    } else {
                        pendingTasksCount++;
                        orderPending++;
                    }
                }

                if (wo.getStatus() == WorkOrderStatus.PENDING || wo.getStatus() == WorkOrderStatus.IN_PROGRESS) {
                    activeVehicleIds.add(wo.getVehicleId().value());
                    assignedItems.add(new AssignedWorkOrderItem(
                            wo.getId().value(),
                            wo.getBranchId().value(),
                            wo.getVehicleId().value(),
                            wo.getInternalNumber(),
                            wo.getStatus().name(),
                            myTasks.size(),
                            orderPending
                    ));
                }
            }
        }

        int dtcAlertsCount = activeVehicleIds.stream()
                .distinct()
                .mapToInt(vehicleId -> dtcAlertQueryService.handle(new GetVehicleDtcAlertHistoryQuery(
                        new com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId(vehicleId))).size())
                .sum();

        return Optional.of(new EmployeeDashboardSummary(
                employeeUuid,
                branchIds,
                assignedItems.size(),
                pendingTasksCount,
                completedTasksCount,
                dtcAlertsCount,
                assignedItems.stream().limit(PREVIEW_LIMIT).toList()
        ));
    }
}
