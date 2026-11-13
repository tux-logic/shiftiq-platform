package com.tuxlogic.shiftiq.platform.iam.interfaces.rest;

import com.tuxlogic.shiftiq.platform.iam.application.internal.commandservices.WorkshopRoleCommandServiceImpl;
import com.tuxlogic.shiftiq.platform.iam.application.internal.queryservices.WorkshopRoleQueryServiceImpl;
import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.WorkshopRole;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Permission;
import com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources.AssignWorkshopRoleResource;
import com.tuxlogic.shiftiq.platform.iam.interfaces.rest.resources.CreateWorkshopRoleResource;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.AuthenticatedPrincipal;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkshopRolesControllerTest {

    @Mock
    private WorkshopRoleCommandServiceImpl commandService;

    @Mock
    private WorkshopRoleQueryServiceImpl queryService;

    @Mock
    private MultiTenancySecurityService securityService;

    private WorkshopRolesController controller;
    private final UUID workshopId = UUID.randomUUID();
    private final UUID actorId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        controller = new WorkshopRolesController(commandService, queryService, securityService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateActor(Set<String> permissions) {
        var principal = Mockito.mock(AuthenticatedPrincipal.class);
        Mockito.lenient().when(principal.getId()).thenReturn(actorId);
        Mockito.lenient().when(principal.getPermissions()).thenReturn(permissions);
        Mockito.lenient().when(principal.hasRole(anyString())).thenReturn(false);
        Mockito.lenient().when(principal.hasPermission(anyString()))
                .thenAnswer(inv -> permissions.contains(inv.getArgument(0)));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );
    }

    @Test
    @DisplayName("POST /roles creates custom role and returns 201 Created")
    void createRoleReturnsCreated() {
        authenticateActor(Set.of("ROLE_MANAGE", "INVENTORY_READ", "INVENTORY_MANAGE"));
        var created = WorkshopRole.createCustom(
                workshopId, "Warehouse", "Warehouse staff", Set.of(Permission.INVENTORY_READ, Permission.INVENTORY_MANAGE));
        when(commandService.createCustomRole(eq(workshopId), eq(actorId), anySet(), eq(false),
                eq("Warehouse"), eq("Warehouse staff"), anySet())).thenReturn(created);

        var response = controller.createRole(workshopId,
                new CreateWorkshopRoleResource("Warehouse", "Warehouse staff", Set.of("INVENTORY_READ", "INVENTORY_MANAGE")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Warehouse");
        verify(securityService).validateWorkshopPermission(workshopId, Permission.ROLE_MANAGE);
    }

    @Test
    @DisplayName("POST /roles denies cross-tenant or unauthorized callers with AccessDeniedException")
    void createRoleDeniesCrossTenantCaller() {
        doThrow(new AccessDeniedException("Missing permission ROLE_MANAGE"))
                .when(securityService).validateWorkshopPermission(workshopId, Permission.ROLE_MANAGE);

        assertThrows(AccessDeniedException.class, () -> controller.createRole(
                workshopId, new CreateWorkshopRoleResource("Cashier", null, Set.of("CHECKOUT_PROCESS"))));
        verifyNoInteractions(commandService);
    }

    @Test
    @DisplayName("DELETE /roles/{roleId} and POST /roles/{roleId}/assignments return 204 No Content")
    void deactivateAndAssignReturnNoContent() {
        authenticateActor(Set.of("ROLE_MANAGE", "INVENTORY_MANAGE"));
        UUID roleId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        var deleteResp = controller.deactivateRole(workshopId, roleId);
        assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(commandService).deactivateCustomRole(workshopId, roleId, actorId);

        var assignResp = controller.assignRole(workshopId, roleId, new AssignWorkshopRoleResource(targetUserId));
        assertThat(assignResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(commandService).assignRoleToUser(eq(workshopId), eq(roleId), eq(targetUserId), eq(actorId), anySet(), eq(false));
    }
}
