package com.tuxlogic.shiftiq.platform.shared.infrastructure.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiTenancySecurityServiceTest {

    @Mock
    private TenantScopeResolver tenantScopeResolver;

    private MultiTenancySecurityService service;

    private final UUID userId = UUID.randomUUID();
    private final UUID ownBranchId = UUID.randomUUID();
    private final UUID otherBranchId = UUID.randomUUID();
    private final UUID ownWorkshopId = UUID.randomUUID();
    private final UUID otherWorkshopId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new MultiTenancySecurityService(tenantScopeResolver);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("branch access is denied when there is no authentication")
    void branchAccessDeniedWithoutAuthentication() {
        assertThat(service.isAuthorizedForBranch(ownBranchId)).isFalse();
    }

    @Test
    @DisplayName("branch access fails closed on a null branch id")
    void branchAccessFailsClosedOnNullBranchId() {
        authenticate(principal(false, true, Set.of()));

        assertThat(service.isAuthorizedForBranch((UUID) null)).isFalse();
        assertThat(service.isAuthorizedForBranch((com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId) null)).isFalse();
    }

    @Test
    @DisplayName("platform admin keeps unrestricted branch access")
    void adminHasUnrestrictedBranchAccess() {
        authenticate(principal(true, false, Set.of()));

        assertThat(service.isAuthorizedForBranch(otherBranchId)).isTrue();
        assertThat(service.resolveNetworkAccessibleBranchIds()).isNull();
    }

    @Test
    @DisplayName("owner only reaches the branches of its own workshops")
    void ownerIsScopedToOwnWorkshopBranches() {
        when(tenantScopeResolver.findBranchIdsForUser(userId)).thenReturn(Set.of(ownBranchId));
        authenticate(principal(false, true, Set.of()));

        assertThat(service.isAuthorizedForBranch(ownBranchId)).isTrue();
        assertThat(service.isAuthorizedForBranch(otherBranchId)).isFalse();
    }

    @Test
    @DisplayName("owner without workshops is denied on every branch")
    void ownerWithoutScopeIsDenied() {
        when(tenantScopeResolver.findBranchIdsForUser(userId)).thenReturn(Set.of());
        authenticate(principal(false, true, Set.of()));

        assertThat(service.isAuthorizedForBranch(otherBranchId)).isFalse();
    }

    @Test
    @DisplayName("owner also reaches branches explicitly assigned to it")
    void ownerReachesExplicitlyAssignedBranches() {
        when(tenantScopeResolver.findBranchIdsForUser(userId)).thenReturn(Set.of(ownBranchId));
        authenticate(principal(false, true, Set.of(otherBranchId)));

        assertThat(service.isAuthorizedForBranch(ownBranchId)).isTrue();
        assertThat(service.isAuthorizedForBranch(otherBranchId)).isTrue();
        assertThat(service.isAuthorizedForBranch(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("employee relies solely on explicit branch membership")
    void employeeReliesOnExplicitMembership() {
        authenticate(principal(false, false, Set.of(ownBranchId)));

        assertThat(service.isAuthorizedForBranch(ownBranchId)).isTrue();
        assertThat(service.isAuthorizedForBranch(otherBranchId)).isFalse();
        Mockito.verifyNoInteractions(tenantScopeResolver);
    }

    @Test
    @DisplayName("owner workshop access is limited to its own workshops")
    void ownerWorkshopAccessIsScoped() {
        when(tenantScopeResolver.findWorkshopIdsForUser(userId)).thenReturn(Set.of(ownWorkshopId));
        authenticate(principal(false, true, Set.of()));

        assertThat(service.isAuthorizedForWorkshop(ownWorkshopId)).isTrue();
        assertThat(service.isAuthorizedForWorkshop(otherWorkshopId)).isFalse();
        assertThat(service.isAuthorizedForWorkshop(null)).isFalse();
    }

    @Test
    @DisplayName("network scope is the union of assigned and owned branches for an owner")
    void networkScopeForOwnerIsAssignedPlusOwnedBranches() {
        when(tenantScopeResolver.findBranchIdsForUser(userId)).thenReturn(Set.of(ownBranchId));
        authenticate(principal(false, true, Set.of(otherBranchId)));

        assertThat(service.resolveNetworkAccessibleBranchIds())
                .containsExactlyInAnyOrder(ownBranchId, otherBranchId);
    }

    @Test
    @DisplayName("network scope is empty for an unauthenticated caller")
    void networkScopeWhenUnauthenticatedIsEmpty() {
        assertThat(service.resolveNetworkAccessibleBranchIds()).isNotNull().isEmpty();
    }

    private AuthenticatedPrincipal principal(boolean admin, boolean owner, Set<UUID> memberships) {
        var principal = Mockito.mock(AuthenticatedPrincipal.class);
        Mockito.lenient().when(principal.getId()).thenReturn(userId);
        Mockito.lenient().when(principal.hasRole("ROLE_ADMIN")).thenReturn(admin);
        Mockito.lenient().when(principal.hasRole("ROLE_OWNER")).thenReturn(owner);
        Mockito.lenient().when(principal.getBranchIds()).thenReturn(memberships);
        Mockito.lenient().when(principal.hasBranch(Mockito.any(UUID.class)))
                .thenAnswer(invocation -> memberships.contains(invocation.getArgument(0)));
        return principal;
    }

    private void authenticate(AuthenticatedPrincipal principal) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }
}
