package com.tuxlogic.shiftiq.platform.fleet.interfaces.rest;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;
import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.EmployeeRegistrationCommandFailure;
import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.EmployeeRegistrationCommandService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.EmployeeRegistrationQueryFailure;
import com.tuxlogic.shiftiq.platform.fleet.application.queryservices.EmployeeRegistrationQueryService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.EmployeeRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.DeleteEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.UpdateEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationByIdQuery;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.resources.UpdateEmployeeRegistrationResource;
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

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeRegistrationsControllerTest {

    @Mock
    private EmployeeRegistrationCommandService commandService;

    @Mock
    private EmployeeRegistrationQueryService queryService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private MultiTenancySecurityService multiTenancySecurityService;

    private EmployeeRegistrationsController controller;

    @BeforeEach
    void setUp() {
        controller = new EmployeeRegistrationsController(commandService, queryService, messageSource, multiTenancySecurityService);
    }

    @Test
    void getById_WhenForbidden_ShouldReturnForbiddenStatus() {
        UUID id = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        EmployeeRegistration registration = new EmployeeRegistration(
                id,
                new BranchId(branchId),
                "MECANICO",
                "Mecánico automotriz",
                new BigDecimal("1500.00")
        );

        when(queryService.handle(any(GetEmployeeRegistrationByIdQuery.class))).thenReturn(Result.success(registration));
        when(multiTenancySecurityService.isAuthorizedForBranch(branchId)).thenReturn(false);

        ResponseEntity<?> response = controller.getById(id);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getRegistrations_WhenBranchForbidden_ShouldReturnForbiddenStatus() {
        UUID branchId = UUID.randomUUID();
        when(multiTenancySecurityService.isAuthorizedForBranch(branchId)).thenReturn(false);

        ResponseEntity<?> response = controller.getRegistrations(branchId, null, null);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void updateEmployeeRegistration_WhenForbidden_ShouldReturnForbiddenStatus() {
        UUID id = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        EmployeeRegistration registration = new EmployeeRegistration(
                id,
                new BranchId(branchId),
                "MECANICO",
                "Mecánico automotriz",
                new BigDecimal("1500.00")
        );

        UpdateEmployeeRegistrationResource resource = new UpdateEmployeeRegistrationResource("ELECTRICISTA", "Electricista automotriz", new BigDecimal("2000.00"));

        when(queryService.handle(any(GetEmployeeRegistrationByIdQuery.class))).thenReturn(Result.success(registration));
        when(multiTenancySecurityService.isAuthorizedForBranch(branchId)).thenReturn(false);

        ResponseEntity<?> response = controller.updateEmployeeRegistration(id, resource);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void updateEmployeeRegistration_WhenAuthorized_ShouldReturnOk() {
        UUID id = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        EmployeeRegistration registration = new EmployeeRegistration(
                id,
                new BranchId(branchId),
                "MECANICO",
                "Mecánico automotriz",
                new BigDecimal("1500.00")
        );

        UpdateEmployeeRegistrationResource resource = new UpdateEmployeeRegistrationResource("ELECTRICISTA", "Electricista automotriz", new BigDecimal("2000.00"));

        when(queryService.handle(any(GetEmployeeRegistrationByIdQuery.class))).thenReturn(Result.success(registration));
        when(multiTenancySecurityService.isAuthorizedForBranch(branchId)).thenReturn(true);
        when(commandService.handle(any(UpdateEmployeeRegistrationCommand.class))).thenReturn(Result.success(registration));

        ResponseEntity<?> response = controller.updateEmployeeRegistration(id, resource);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deactivateEmployeeRegistration_WhenForbidden_ShouldReturnForbiddenStatus() {
        UUID id = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        EmployeeRegistration registration = new EmployeeRegistration(
                id,
                new BranchId(branchId),
                "MECANICO",
                "Mecánico automotriz",
                new BigDecimal("1500.00")
        );

        when(queryService.handle(any(GetEmployeeRegistrationByIdQuery.class))).thenReturn(Result.success(registration));
        when(multiTenancySecurityService.isAuthorizedForBranch(branchId)).thenReturn(false);

        ResponseEntity<?> response = controller.deactivateEmployeeRegistration(id);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void deactivateEmployeeRegistration_WhenAuthorized_ShouldReturnNoContent() {
        UUID id = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        EmployeeRegistration registration = new EmployeeRegistration(
                id,
                new BranchId(branchId),
                "MECANICO",
                "Mecánico automotriz",
                new BigDecimal("1500.00")
        );

        when(queryService.handle(any(GetEmployeeRegistrationByIdQuery.class))).thenReturn(Result.success(registration));
        when(multiTenancySecurityService.isAuthorizedForBranch(branchId)).thenReturn(true);
        when(commandService.handle(any(DeleteEmployeeRegistrationCommand.class))).thenReturn(Result.success(registration));

        ResponseEntity<?> response = controller.deactivateEmployeeRegistration(id);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}
