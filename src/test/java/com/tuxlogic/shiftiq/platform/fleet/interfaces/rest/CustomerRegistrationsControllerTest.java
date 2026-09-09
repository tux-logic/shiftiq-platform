package com.tuxlogic.shiftiq.platform.fleet.interfaces.rest;

import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.CustomerRegistrationCommandService;
import com.tuxlogic.shiftiq.platform.fleet.application.queryservices.CustomerRegistrationQueryService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.CustomerRegistration;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.resources.UpdateCustomerRegistrationResource;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerRegistrationsControllerTest {

    @Mock
    private CustomerRegistrationCommandService commandService;

    @Mock
    private CustomerRegistrationQueryService queryService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private MultiTenancySecurityService multiTenancySecurityService;

    private CustomerRegistrationsController controller;

    @BeforeEach
    void setUp() {
        controller = new CustomerRegistrationsController(commandService, queryService, messageSource, multiTenancySecurityService);
    }

    @Test
    void getById_WhenForbidden_ShouldReturnForbiddenStatus() {
        UUID registrationId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        CustomerRegistration reg = new CustomerRegistration(UUID.randomUUID(), new BranchId(branchId));

        when(queryService.handle(registrationId)).thenReturn(Result.success(reg));
        when(multiTenancySecurityService.isAuthorizedForBranch(branchId)).thenReturn(false);

        ResponseEntity<?> response = controller.getById(registrationId);

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
    void update_WhenForbidden_ShouldReturnForbiddenStatus() {
        UUID registrationId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        CustomerRegistration reg = new CustomerRegistration(UUID.randomUUID(), new BranchId(branchId));
        UpdateCustomerRegistrationResource resource = new UpdateCustomerRegistrationResource("INACTIVE");

        when(queryService.handle(registrationId)).thenReturn(Result.success(reg));
        when(multiTenancySecurityService.isAuthorizedForBranch(branchId)).thenReturn(false);

        ResponseEntity<?> response = controller.update(registrationId, resource);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void delete_WhenForbidden_ShouldReturnForbiddenStatus() {
        UUID registrationId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        CustomerRegistration reg = new CustomerRegistration(UUID.randomUUID(), new BranchId(branchId));

        when(queryService.handle(registrationId)).thenReturn(Result.success(reg));
        when(multiTenancySecurityService.isAuthorizedForBranch(branchId)).thenReturn(false);

        ResponseEntity<?> response = controller.delete(registrationId);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
