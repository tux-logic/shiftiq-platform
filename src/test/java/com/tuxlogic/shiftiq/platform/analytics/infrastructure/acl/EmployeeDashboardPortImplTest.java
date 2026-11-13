package com.tuxlogic.shiftiq.platform.analytics.infrastructure.acl;

import com.tuxlogic.shiftiq.platform.core.application.queryservices.EmployeeQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Employee;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetEmployeeByUserIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.UserId;
import com.tuxlogic.shiftiq.platform.fleet.application.queryservices.EmployeeRegistrationQueryService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.EmployeeRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationByEmployeeIdQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.EmployeeRegistrationStatus;
import com.tuxlogic.shiftiq.platform.iot.application.queryservices.DtcAlertQueryService;
import com.tuxlogic.shiftiq.platform.iot.domain.model.aggregates.DtcAlert;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetVehicleDtcAlertHistoryQuery;
import com.tuxlogic.shiftiq.platform.operations.application.queryservices.WorkOrderQueryService;
import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.WorkOrder;
import com.tuxlogic.shiftiq.platform.operations.domain.model.entities.WorkOrderTask;
import com.tuxlogic.shiftiq.platform.operations.domain.model.queries.GetWorkOrdersByBranchIdQuery;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.MechanicId;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderId;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderStatus;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderTaskStatus;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmployeeDashboardPortImplTest {

    private EmployeeQueryService employeeQueryService;
    private EmployeeRegistrationQueryService employeeRegistrationQueryService;
    private WorkOrderQueryService workOrderQueryService;
    private DtcAlertQueryService dtcAlertQueryService;
    private EmployeeDashboardPortImpl port;

    @BeforeEach
    void setUp() {
        employeeQueryService = mock(EmployeeQueryService.class);
        employeeRegistrationQueryService = mock(EmployeeRegistrationQueryService.class);
        workOrderQueryService = mock(WorkOrderQueryService.class);
        dtcAlertQueryService = mock(DtcAlertQueryService.class);
        port = new EmployeeDashboardPortImpl(
                employeeQueryService,
                employeeRegistrationQueryService,
                workOrderQueryService,
                dtcAlertQueryService
        );
    }

    @Test
    @DisplayName("returns empty when no employee profile exists for userId")
    void returnsEmptyWhenEmployeeNotFound() {
        UUID userId = UUID.randomUUID();
        when(employeeQueryService.handle(new GetEmployeeByUserIdQuery(new UserId(userId))))
                .thenReturn(Optional.empty());

        assertThat(port.loadSummaryForUser(userId)).isEmpty();
    }

    @Test
    @DisplayName("aggregates assigned work orders, task counts, and vehicle DTC alerts for mechanic")
    void aggregatesEmployeeDashboardSummary() {
        UUID userId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UUID workOrderId = UUID.randomUUID();

        Employee employee = mock(Employee.class);
        when(employee.getId()).thenReturn(new EmployeeId(employeeId));
        when(employeeQueryService.handle(new GetEmployeeByUserIdQuery(new UserId(userId))))
                .thenReturn(Optional.of(employee));

        EmployeeRegistration reg = mock(EmployeeRegistration.class);
        when(reg.getStatus()).thenReturn(EmployeeRegistrationStatus.ACTIVE);
        when(reg.getBranchId()).thenReturn(new BranchId(branchId));
        when(employeeRegistrationQueryService.handle(new GetEmployeeRegistrationByEmployeeIdQuery(employeeId)))
                .thenReturn(Result.success(reg));

        WorkOrderTask pendingTask = mock(WorkOrderTask.class);
        when(pendingTask.getAssignedMechanicId()).thenReturn(new MechanicId(employeeId));
        when(pendingTask.getStatus()).thenReturn(WorkOrderTaskStatus.DOING);

        WorkOrderTask completedTask = mock(WorkOrderTask.class);
        when(completedTask.getAssignedMechanicId()).thenReturn(new MechanicId(employeeId));
        when(completedTask.getStatus()).thenReturn(WorkOrderTaskStatus.COMPLETED);

        WorkOrderTask otherMechanicTask = mock(WorkOrderTask.class);
        when(otherMechanicTask.getAssignedMechanicId()).thenReturn(new MechanicId(UUID.randomUUID()));
        when(otherMechanicTask.getStatus()).thenReturn(WorkOrderTaskStatus.PENDING);

        WorkOrder order = mock(WorkOrder.class);
        when(order.getId()).thenReturn(new WorkOrderId(workOrderId));
        when(order.getBranchId()).thenReturn(new BranchId(branchId));
        when(order.getVehicleId()).thenReturn(new VehicleId(vehicleId));
        when(order.getInternalNumber()).thenReturn(205);
        when(order.getStatus()).thenReturn(WorkOrderStatus.IN_PROGRESS);
        when(order.getTasks()).thenReturn(List.of(pendingTask, completedTask, otherMechanicTask));

        when(workOrderQueryService.handle(new GetWorkOrdersByBranchIdQuery(new BranchId(branchId))))
                .thenReturn(List.of(order));
        when(dtcAlertQueryService.handle(new GetVehicleDtcAlertHistoryQuery(new VehicleId(vehicleId))))
                .thenReturn(List.of(mock(DtcAlert.class)));

        var result = port.loadSummaryForUser(userId);

        assertThat(result).isPresent();
        var summary = result.get();
        assertThat(summary.employeeId()).isEqualTo(employeeId);
        assertThat(summary.branchIds()).containsExactly(branchId);
        assertThat(summary.assignedWorkOrdersCount()).isEqualTo(1);
        assertThat(summary.pendingTasksCount()).isEqualTo(1);
        assertThat(summary.completedTasksCount()).isEqualTo(1);
        assertThat(summary.activeDtcAlertsCount()).isEqualTo(1);
        assertThat(summary.assignedWorkOrders()).singleElement().satisfies(item -> {
            assertThat(item.assignedTasksCount()).isEqualTo(2);
            assertThat(item.pendingTasksCount()).isEqualTo(1);
        });
    }
}
