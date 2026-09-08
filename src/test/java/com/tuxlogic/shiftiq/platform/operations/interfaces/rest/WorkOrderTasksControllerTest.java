package com.tuxlogic.shiftiq.platform.operations.interfaces.rest;

import com.tuxlogic.shiftiq.platform.operations.application.commandservices.WorkOrderCommandService;
import com.tuxlogic.shiftiq.platform.operations.application.queryservices.WorkOrderQueryService;
import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.WorkOrder;
import com.tuxlogic.shiftiq.platform.operations.domain.model.commands.AssignMechanicToTaskCommand;
import com.tuxlogic.shiftiq.platform.operations.domain.model.queries.GetWorkOrderByTaskIdQuery;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderId;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkOrderTasksControllerTest {

    @Mock
    private WorkOrderCommandService commandService;

    @Mock
    private WorkOrderQueryService queryService;

    @Mock
    private MultiTenancySecurityService multiTenancySecurityService;

    @Mock
    private MessageSource messageSource;

    private WorkOrderTasksController controller;

    @BeforeEach
    void setUp() {
        controller = new WorkOrderTasksController(commandService, queryService, multiTenancySecurityService, messageSource);
    }

    @Test
    void assignMechanicToTask_WhenTaskAccessValid_ShouldReturnOk() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID mechanicId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        WorkOrder realWorkOrder = new WorkOrder(
                new com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.AppointmentId(UUID.randomUUID()),
                new BranchId(branchId),
                new com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId(UUID.randomUUID()),
                new com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId(UUID.randomUUID()),
                101,
                new com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.DiagnosticSummary("Diagnostico test"),
                new com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Mileage(50000)
        );

        when(queryService.handle(any(GetWorkOrderByTaskIdQuery.class))).thenReturn(Optional.of(realWorkOrder));
        when(commandService.handle(any(AssignMechanicToTaskCommand.class))).thenReturn(Result.success(realWorkOrder));

        // Act
        ResponseEntity<?> response = controller.assignMechanicToTask(taskId, mechanicId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(multiTenancySecurityService).validateBranchAccess(branchId);
        verify(commandService).handle(any(AssignMechanicToTaskCommand.class));
    }
}
