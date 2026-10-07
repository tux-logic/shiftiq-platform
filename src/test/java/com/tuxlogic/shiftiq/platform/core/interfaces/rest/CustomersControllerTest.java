package com.tuxlogic.shiftiq.platform.core.interfaces.rest;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.CustomerCommandService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.CustomerQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Customer;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetCustomerByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.UserId;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.UpdateCustomerResource;
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

class CustomersControllerTest {

    private CustomerCommandService commandService;
    private CustomerQueryService queryService;
    private CustomersController controller;
    private final UUID callerId = UUID.randomUUID();
    private final UUID otherUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        commandService = Mockito.mock(CustomerCommandService.class);
        queryService = Mockito.mock(CustomerQueryService.class);
        var tenantScopeResolver = Mockito.mock(TenantScopeResolver.class);
        controller = new CustomersController(
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

        assertThatThrownBy(() -> controller.updateCustomer(UUID.randomUUID(), updateResource()))
                .isInstanceOf(AccessDeniedException.class);
        Mockito.verifyNoInteractions(commandService);
    }

    @Test
    @DisplayName("PUT allows an owner editing its own profile")
    void ownerCanUpdateOwnProfile() {
        authenticate(callerId, false);
        stubProfileLoadedFor(callerId);
        when(commandService.handle(any(com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateCustomerCommand.class)))
                .thenReturn(Optional.empty());

        var response = controller.updateCustomer(UUID.randomUUID(), updateResource());

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        Mockito.verify(commandService).handle(any(com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateCustomerCommand.class));
    }

    @Test
    @DisplayName("PUT allows a platform admin to edit any profile")
    void adminCanUpdateAnyProfile() {
        authenticate(callerId, true);
        stubProfileLoadedFor(otherUserId);
        when(commandService.handle(any(com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateCustomerCommand.class)))
                .thenReturn(Optional.empty());

        var response = controller.updateCustomer(UUID.randomUUID(), updateResource());

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        Mockito.verify(commandService).handle(any(com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateCustomerCommand.class));
    }

    @Test
    @DisplayName("DELETE denies an owner removing the profile of another user")
    void ownerCannotDeleteAnotherUsersProfile() {
        authenticate(callerId, false);
        stubProfileLoadedFor(otherUserId);

        assertThatThrownBy(() -> controller.deleteCustomer(UUID.randomUUID()))
                .isInstanceOf(AccessDeniedException.class);
        Mockito.verifyNoInteractions(commandService);
    }

    @Test
    @DisplayName("DELETE allows a platform admin to remove any profile")
    void adminCanDeleteAnyProfile() {
        authenticate(callerId, true);
        stubProfileLoadedFor(otherUserId);

        var response = controller.deleteCustomer(UUID.randomUUID());

        assertThat(response.getStatusCode().value()).isEqualTo(204);
    }

    private void stubProfileLoadedFor(UUID profileUserId) {
        var customer = Mockito.mock(Customer.class);
        when(customer.getUserId()).thenReturn(new UserId(profileUserId));
        when(queryService.handle(any(GetCustomerByIdQuery.class))).thenReturn(Optional.of(customer));
    }

    private UpdateCustomerResource updateResource() {
        return new UpdateCustomerResource("Ana", "Perez", null, "DNI", "12345678", "999888777");
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
