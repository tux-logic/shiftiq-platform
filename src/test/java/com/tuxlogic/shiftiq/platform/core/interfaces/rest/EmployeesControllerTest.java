package com.tuxlogic.shiftiq.platform.core.interfaces.rest;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.EmployeeCommandService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.EmployeeQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Employee;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateEmployeeCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetEmployeeByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.UserId;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.UpdateEmployeeResource;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.AuthenticatedPrincipal;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.TenantScopeResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class EmployeesControllerTest {

    private EmployeeCommandService commandService;
    private EmployeeQueryService queryService;
    private EmployeesController controller;
    private final UUID callerId = UUID.randomUUID();
    private final UUID otherUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        commandService = Mockito.mock(EmployeeCommandService.class);
        queryService = Mockito.mock(EmployeeQueryService.class);
        var tenantScopeResolver = Mockito.mock(TenantScopeResolver.class);
        controller = new EmployeesController(
                commandService, queryService, new MultiTenancySecurityService(tenantScopeResolver));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("PUT denies an owner editing the profile of another user")
    void ownerCannotUpdateAnotherUsersProfile() {
        authenticate(callerId, false);
        stubProfileLoadedFor(otherUserId);

        assertThatThrownBy(() -> controller.updateEmployee(UUID.randomUUID(), updateResource()))
                .isInstanceOf(AccessDeniedException.class);
        Mockito.verifyNoInteractions(commandService);
    }

    @Test
    @DisplayName("PUT allows an owner editing its own profile")
    void ownerCanUpdateOwnProfile() {
        authenticate(callerId, false);
        stubProfileLoadedFor(callerId);
        when(commandService.handle(any(UpdateEmployeeCommand.class))).thenReturn(Optional.empty());

        var response = controller.updateEmployee(UUID.randomUUID(), updateResource());

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        Mockito.verify(commandService).handle(any(UpdateEmployeeCommand.class));
    }

    @Test
    @DisplayName("PUT allows a platform admin to edit any profile")
    void adminCanUpdateAnyProfile() {
        authenticate(callerId, true);
        stubProfileLoadedFor(otherUserId);
        when(commandService.handle(any(UpdateEmployeeCommand.class))).thenReturn(Optional.empty());

        var response = controller.updateEmployee(UUID.randomUUID(), updateResource());

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        Mockito.verify(commandService).handle(any(UpdateEmployeeCommand.class));
    }

    @Test
    @DisplayName("DELETE denies an owner removing the profile of another user")
    void ownerCannotDeleteAnotherUsersProfile() {
        authenticate(callerId, false);
        stubProfileLoadedFor(otherUserId);

        assertThatThrownBy(() -> controller.deleteEmployee(UUID.randomUUID()))
                .isInstanceOf(AccessDeniedException.class);
        Mockito.verifyNoInteractions(commandService);
    }

    @Test
    @DisplayName("DELETE allows a platform admin to remove any profile")
    void adminCanDeleteAnyProfile() {
        authenticate(callerId, true);
        stubProfileLoadedFor(otherUserId);

        var response = controller.deleteEmployee(UUID.randomUUID());

        assertThat(response.getStatusCode().value()).isEqualTo(204);
    }

    private void stubProfileLoadedFor(UUID profileUserId) {
        var employee = Mockito.mock(Employee.class);
        when(employee.getUserId()).thenReturn(new UserId(profileUserId));
        when(queryService.handle(any(GetEmployeeByIdQuery.class))).thenReturn(Optional.of(employee));
    }

    private UpdateEmployeeResource updateResource() {
        return new UpdateEmployeeResource("Ana", "Perez", "DNI", "12345678", "999888777");
    }

    private void authenticate(UUID principalId, boolean admin) {
        var principal = Mockito.mock(AuthenticatedPrincipal.class);
        Mockito.lenient().when(principal.getId()).thenReturn(principalId);
        Mockito.lenient().when(principal.hasRole("ROLE_ADMIN")).thenReturn(admin);
        Mockito.lenient().when(principal.hasRole("ROLE_OWNER")).thenReturn(!admin);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, java.util.List.of()));
    }
}
