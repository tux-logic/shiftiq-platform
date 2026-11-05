package com.tuxlogic.shiftiq.platform.operations.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.operations.application.commandservices.WorkOrderCommandFailure;
import com.tuxlogic.shiftiq.platform.operations.application.outboundservices.ExternalEmployeeService;
import com.tuxlogic.shiftiq.platform.operations.application.outboundservices.ExternalProductService;
import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.WorkOrder;
import com.tuxlogic.shiftiq.platform.operations.domain.model.commands.CompleteWorkOrderCommand;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderId;
import com.tuxlogic.shiftiq.platform.operations.domain.repositories.ServiceRepository;
import com.tuxlogic.shiftiq.platform.operations.domain.repositories.WorkOrderRepository;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkOrderCommandServiceImplTest {

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ExternalProductService externalProductService;

    @Mock
    private ExternalEmployeeService externalEmployeeService;

    private WorkOrderCommandServiceImpl commandService;

    @BeforeEach
    void setUp() {
        commandService = new WorkOrderCommandServiceImpl(workOrderRepository, serviceRepository, externalProductService, externalEmployeeService);
    }

    @Test
    void handleCompleteWorkOrder_WhenWorkOrderExists_ShouldCompleteAndSave() {
        // Arrange
        UUID workOrderId = UUID.randomUUID();
        WorkOrder mockWorkOrder = mock(WorkOrder.class);

        when(workOrderRepository.findById(any(WorkOrderId.class))).thenReturn(Optional.of(mockWorkOrder));
        when(workOrderRepository.save(any(WorkOrder.class))).thenReturn(mockWorkOrder);

        CompleteWorkOrderCommand command = new CompleteWorkOrderCommand(new WorkOrderId(workOrderId));

        // Act
        Result<WorkOrder, WorkOrderCommandFailure> result = commandService.handle(command);

        // Assert
        assertTrue(result.isSuccess());
        verify(mockWorkOrder).completeWorkOrder();
        verify(workOrderRepository).save(mockWorkOrder);
    }

    @Test
    void handleCompleteWorkOrder_WhenWorkOrderNotFound_ShouldReturnFailure() {
        // Arrange
        UUID workOrderId = UUID.randomUUID();
        when(workOrderRepository.findById(any(WorkOrderId.class))).thenReturn(Optional.empty());

        CompleteWorkOrderCommand command = new CompleteWorkOrderCommand(new WorkOrderId(workOrderId));

        // Act
        Result<WorkOrder, WorkOrderCommandFailure> result = commandService.handle(command);

        // Assert
        assertTrue(result.isFailure());
        assertInstanceOf(WorkOrderCommandFailure.NotFound.class, result.failure().get());
        verify(workOrderRepository, never()).save(any());
    }

    @Test
    void handleAssignMechanicToTask_ShouldResolveMechanicRegistrationId() {
        // Arrange
        UUID workOrderId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID candidateMechanicId = UUID.randomUUID();
        UUID resolvedRegistrationId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        WorkOrder mockWorkOrder = mock(WorkOrder.class);
        when(mockWorkOrder.getBranchId()).thenReturn(new com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId(branchId));
        when(workOrderRepository.findById(any(WorkOrderId.class))).thenReturn(Optional.of(mockWorkOrder));
        when(workOrderRepository.save(any(WorkOrder.class))).thenReturn(mockWorkOrder);
        when(externalEmployeeService.resolveActiveRegistrationId(candidateMechanicId, branchId)).thenReturn(resolvedRegistrationId);

        var command = new com.tuxlogic.shiftiq.platform.operations.domain.model.commands.AssignMechanicToTaskCommand(
                new WorkOrderId(workOrderId),
                new com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderTaskId(taskId),
                new com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.MechanicId(candidateMechanicId)
        );

        // Act
        Result<WorkOrder, WorkOrderCommandFailure> result = commandService.handle(command);

        // Assert
        assertTrue(result.isSuccess());
        verify(mockWorkOrder).assignMechanicToTask(any(), eq(new com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.MechanicId(resolvedRegistrationId)));
        verify(workOrderRepository).save(mockWorkOrder);
    }
}
