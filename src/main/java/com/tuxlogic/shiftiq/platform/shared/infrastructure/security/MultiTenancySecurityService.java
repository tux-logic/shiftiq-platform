package com.tuxlogic.shiftiq.platform.shared.infrastructure.security;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Service to validate multi-tenancy access ensuring requested branchId is validated against the authenticated user session.
 *
 * <p>All checks are <b>fail closed</b>: a missing identifier (null) is denied instead of
 * allowed, so omitting {@code branchId}/{@code userId} from a payload can never bypass
 * tenant isolation.</p>
 *
 * <p>{@code ROLE_ADMIN} is the platform operator and keeps unrestricted access.
 * {@code ROLE_OWNER} is scoped to the workshops it owns (resolved through
 * {@link TenantScopeResolver}) plus the branches explicitly assigned to it, so a SaaS
 * deployment with several independent workshop owners never reads another tenant's data.</p>
 */
@Service("multiTenancySecurityService")
public class MultiTenancySecurityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiTenancySecurityService.class);

    private final TenantScopeResolver tenantScopeResolver;

    public MultiTenancySecurityService(TenantScopeResolver tenantScopeResolver) {
        this.tenantScopeResolver = tenantScopeResolver;
    }

    public boolean isAuthorizedForBranch(UUID branchId) {
        if (branchId == null) {
            LOGGER.warn("Branch access denied: no branch identifier was provided");
            return false;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedPrincipal authenticatedPrincipal) {
            if (authenticatedPrincipal.hasRole("ROLE_ADMIN")) {
                return true;
            }
            if (authenticatedPrincipal.hasRole("ROLE_OWNER")) {
                return authenticatedPrincipal.hasBranch(branchId)
                        || tenantScopeResolver.findBranchIdsForUser(authenticatedPrincipal.getId()).contains(branchId);
            }
            return authenticatedPrincipal.hasBranch(branchId);
        }
        return false;
    }

    /**
     * Convenience overload for aggregates whose branch may be absent: it fails closed
     * instead of letting branch-less records slip through the check.
     */
    public boolean isAuthorizedForBranch(BranchId branchId) {
        return isAuthorizedForBranch(branchId == null ? null : branchId.value());
    }

    public void validateBranchAccess(UUID branchId) {
        if (!isAuthorizedForBranch(branchId)) {
            throw new AccessDeniedException("Unauthorized access for requested branch identifier: " + branchId);
        }
    }

    /**
     * Broad check for resources keyed by a user id where no tenant scoping exists in the
     * data model (customer and employee profiles are global records with no branch or
     * workshop owner). Allows the caller itself, {@code ROLE_ADMIN} and, because those
     * profiles are created by the workshop during onboarding, {@code ROLE_OWNER}.
     * For owner profiles or tenant scoped data use {@link #isAuthorizedForOwnerProfile(UUID)}
     * or {@link #isAuthorizedForSelf(UUID)} instead.
     */
    public boolean isAuthorizedForUser(UUID userId) {
        if (userId == null) {
            LOGGER.warn("User access denied: no user identifier was provided");
            return false;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedPrincipal authenticatedPrincipal) {
            if (authenticatedPrincipal.getId().equals(userId)) {
                return true;
            }
            return authenticatedPrincipal.hasRole("ROLE_ADMIN") || authenticatedPrincipal.hasRole("ROLE_OWNER");
        }
        return false;
    }

    public void validateUserAccess(UUID userId) {
        if (!isAuthorizedForUser(userId)) {
            throw new AccessDeniedException("Unauthorized access for requested user identifier: " + userId);
        }
    }

    /**
     * Authorization for operations over an owner profile (tenant onboarding data).
     * Unlike {@link #isAuthorizedForUser(UUID)} it does not let {@code ROLE_OWNER}
     * cross tenants: only {@code ROLE_ADMIN} or the owner whose user matches the
     * profile may proceed.
     */
    public boolean isAuthorizedForOwnerProfile(UUID ownerId) {
        if (ownerId == null) {
            LOGGER.warn("Owner profile access denied: no owner identifier was provided");
            return false;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedPrincipal authenticatedPrincipal) {
            if (authenticatedPrincipal.hasRole("ROLE_ADMIN")) {
                return true;
            }
            return ownerId.equals(tenantScopeResolver.findOwnerProfileIdForUser(authenticatedPrincipal.getId()));
        }
        return false;
    }

    public void validateOwnerProfileAccess(UUID ownerId) {
        if (!isAuthorizedForOwnerProfile(ownerId)) {
            throw new AccessDeniedException("Unauthorized access for requested owner identifier: " + ownerId);
        }
    }

    /**
     * Authorization restricted to the caller itself (or a platform admin). Intended for
     * resources keyed by the authenticated user, where the {@code ROLE_OWNER} bypass of
     * {@link #isAuthorizedForUser(UUID)} would allow crossing tenants.
     */
    public boolean isAuthorizedForSelf(UUID userId) {
        if (userId == null) {
            LOGGER.warn("Self access denied: no user identifier was provided");
            return false;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedPrincipal authenticatedPrincipal) {
            if (authenticatedPrincipal.hasRole("ROLE_ADMIN")) {
                return true;
            }
            return authenticatedPrincipal.getId().equals(userId);
        }
        return false;
    }

    public void validateSelfAccess(UUID userId) {
        if (!isAuthorizedForSelf(userId)) {
            throw new AccessDeniedException("Unauthorized access for requested user identifier: " + userId);
        }
    }

    public boolean isAuthorizedForWorkshop(UUID workshopId) {
        if (workshopId == null) {
            LOGGER.warn("Workshop access denied: no workshop identifier was provided");
            return false;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedPrincipal authenticatedPrincipal) {
            if (authenticatedPrincipal.hasRole("ROLE_ADMIN")) {
                return true;
            }
            if (authenticatedPrincipal.hasRole("ROLE_OWNER")) {
                return tenantScopeResolver.findWorkshopIdsForUser(authenticatedPrincipal.getId()).contains(workshopId);
            }
            return false;
        }
        return false;
    }

    public void validateWorkshopAccess(UUID workshopId) {
        if (!isAuthorizedForWorkshop(workshopId)) {
            throw new AccessDeniedException("Unauthorized access for requested workshop identifier: " + workshopId);
        }
    }

    /**
     * Resolves the branch scope a caller may aggregate over in network-wide metrics.
     *
     * <p>The returned set is never {@code null}: {@code ROLE_ADMIN} resolves to every
     * branch of the platform, other callers resolve to the branches they may access, and
     * an unauthenticated caller (or a caller without any branch) resolves to an empty set
     * so aggregation always runs over an explicit scope.</p>
     *
     * @return the branch ids the caller may see, empty when the caller may see none
     */
    public Set<UUID> resolveNetworkAccessibleBranchIds() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Set.of();
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof AuthenticatedPrincipal authenticatedPrincipal)) {
            return Set.of();
        }
        if (authenticatedPrincipal.hasRole("ROLE_ADMIN")) {
            return tenantScopeResolver.findAllBranchIds();
        }
        Set<UUID> accessible = new HashSet<>(authenticatedPrincipal.getBranchIds());
        if (authenticatedPrincipal.hasRole("ROLE_OWNER")) {
            accessible.addAll(tenantScopeResolver.findBranchIdsForUser(authenticatedPrincipal.getId()));
        }
        return accessible;
    }

    /**
     * Checks that a branch exists (and is not soft deleted) regardless of who calls.
     * Used to answer {@code 404} for unknown branches after the access check passed.
     */
    public boolean branchExists(UUID branchId) {
        return branchId != null && tenantScopeResolver.branchExists(branchId);
    }
}
