package com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates;

import com.tuxlogic.shiftiq.platform.operations.domain.model.events.WorkOrderCompletedEvent;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.AppointmentId;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.MechanicId;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.ServiceId;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.TaskDescription;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderStatus;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Mileage;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkOrderTest {

    private WorkOrder workOrder;

    @BeforeEach
    void setUp() {
        workOrder = new WorkOrder(
                new AppointmentId(UUID.randomUUID()),
                new BranchId(UUID.randomUUID()),
                new VehicleId(UUID.randomUUID()),
                new CustomerId(UUID.randomUUID()),
                101,
                new DiagnosticSummary("Diagnostico inicial"),
                new Mileage(15000)
        );
        workOrder.addTask(
                new ServiceId(UUID.randomUUID()),
                new MechanicId(UUID.randomUUID()),
                new TaskDescription("Cambio de aceite"),
                new Money(new BigDecimal("100.00"))
        );
    }

    @Test
    @DisplayName("Completing the last task completes the order and registers WorkOrderCompletedEvent")
    void completingLastTaskCompletesOrderAndRegistersWorkOrderCompletedEvent() {
        var taskId = workOrder.getTasks().get(0).getId();

        workOrder.startTask(taskId);
        workOrder.completeTask(taskId);

        assertThat(workOrder.getStatus()).isEqualTo(WorkOrderStatus.COMPLETED);
        assertThat(completedEvents()).hasSize(1);
    }

    @Test
    @DisplayName("completeWorkOrder after auto-completion does not register a second WorkOrderCompletedEvent")
    void completeWorkOrderAfterAutoCompletionIsIdempotent() {
        var taskId = workOrder.getTasks().get(0).getId();
        workOrder.startTask(taskId);
        workOrder.completeTask(taskId);
        workOrder.clearDomainEvents();

        workOrder.completeWorkOrder();

        assertThat(workOrder.getStatus()).isEqualTo(WorkOrderStatus.COMPLETED);
        assertThat(completedEvents()).isEmpty();
    }

    @Test
    @DisplayName("completeWorkOrder still rejects an order with pending tasks")
    void completeWorkOrderWithPendingTasksThrows() {
        assertThatThrownBy(workOrder::completeWorkOrder)
                .isInstanceOf(IllegalStateException.class);
        assertThat(workOrder.getStatus()).isEqualTo(WorkOrderStatus.PENDING);
    }

    @Test
    @DisplayName("Reopening a completed order registers WorkOrderReopenedEvent and reverts it to IN_PROGRESS")
    void reopeningCompletedOrderRegistersWorkOrderReopenedEvent() {
        var taskId = workOrder.getTasks().get(0).getId();
        workOrder.startTask(taskId);
        workOrder.completeTask(taskId);
        workOrder.clearDomainEvents();

        workOrder.reopenTask(taskId);

        assertThat(workOrder.getStatus()).isEqualTo(WorkOrderStatus.IN_PROGRESS);
        assertThat(reopenedEvents()).hasSize(1);
        assertThat(completedEvents()).isEmpty();
    }

    @Test
    @DisplayName("Reopening a task of an order that is still in progress does not register WorkOrderReopenedEvent")
    void reopeningTaskOfUncompletedOrderDoesNotRegisterReopenedEvent() {
        workOrder.addTask(
                new ServiceId(UUID.randomUUID()),
                new MechanicId(UUID.randomUUID()),
                new TaskDescription("Rotacion de llantas"),
                new Money(new BigDecimal("50.00"))
        );
        var firstTask = workOrder.getTasks().get(0).getId();
        workOrder.startTask(firstTask);
        workOrder.completeTask(firstTask);
        assertThat(workOrder.getStatus()).isEqualTo(WorkOrderStatus.IN_PROGRESS);
        workOrder.clearDomainEvents();

        workOrder.reopenTask(firstTask);

        assertThat(workOrder.getStatus()).isEqualTo(WorkOrderStatus.IN_PROGRESS);
        assertThat(reopenedEvents()).isEmpty();
    }

    private List<Object> reopenedEvents() {
        return workOrder.domainEvents().stream()
                .filter(com.tuxlogic.shiftiq.platform.operations.domain.model.events.WorkOrderReopenedEvent.class::isInstance)
                .toList();
    }

    private List<Object> completedEvents() {
        return workOrder.domainEvents().stream()
                .filter(WorkOrderCompletedEvent.class::isInstance)
                .toList();
    }
}
