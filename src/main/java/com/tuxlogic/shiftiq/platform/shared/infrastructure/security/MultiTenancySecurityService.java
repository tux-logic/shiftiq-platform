package com.tuxlogic.shiftiq.platform.shared.infrastructure.security;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
    private final AccountBranchScopeResolver accountBranchScopeResolver;

    public MultiTenancySecurityService(TenantScopeResolver tenantScopeResolver,
                                       AccountBranchScopeResolver accountBranchScopeResolver) {
        this.tenantScopeResolver = tenantScopeResolver;
        this.accountBranchScopeResolver = accountBranchScopeResolver;
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
     * workshop owner). Allows the caller itself, {@code ROLE_ADMIN}, {@code ROLE_OWNER}
     * (global onboarding bypass) and, scoped to shared branches, {@code ROLE_BRANCH_MANAGER}
     * and {@code ROLE_ASSISTANT} so workshop staff can manage only the accounts that belong
     * to one of their own branches.
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
            if (authenticatedPrincipal.hasRole("ROLE_ADMIN") || authenticatedPrincipal.hasRole("ROLE_OWNER")) {
                return true;
            }
            if (authenticatedPrincipal.hasRole("ROLE_BRANCH_MANAGER") || authenticatedPrincipal.hasRole("ROLE_ASSISTANT")) {
                return sharesBranchWith(authenticatedPrincipal, userId);
            }
            return false;
        }
        return false;
    }

    /**
     * @param authenticatedPrincipal caller requesting access to another account
     * @param userId                 account being accessed
     * @return {@code true} only when both accounts belong to at least one common branch
     */
    private boolean sharesBranchWith(AuthenticatedPrincipal authenticatedPrincipal, UUID userId) {
        Set<UUID> callerBranches = authenticatedPrincipal.getBranchIds();
        if (callerBranches == null || callerBranches.isEmpty()) {
            return false;
        }
        Set<UUID> targetBranches = accountBranchScopeResolver.findBranchIdsForAccount(userId);
        if (targetBranches == null || targetBranches.isEmpty()) {
            return false;
        }
        return !Collections.disjoint(callerBranches, targetBranches);
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
            if (authenticatedPrincipal.hasRole("ROLE_BRANCH_MANAGER") || authenticatedPrincipal.hasRole("ROLE_ASSISTANT")) {
                for (UUID assignedBranchId : authenticatedPrincipal.getBranchIds()) {
                    if (workshopId.equals(tenantScopeResolver.findWorkshopIdForBranch(assignedBranchId))) {
                        return true;
                    }
                }
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
     * Enforces the hierarchical onboarding and staff management rules for a branch:
     * - ADMIN: can manage any staff role.
     * - OWNER:
     *     - Must own the branch.
     *     - Can assign ROLE_BRANCH_MANAGER.
     *     - In single-branch workshops (branch count &lt;= 1), the owner acts as manager
     *       and can assign ROLE_ASSISTANT (or ROLE_EMPLOYEE directly).
     *     - In multi-branch workshops, delegation is enforced: OWNER assigns the BRANCH_MANAGER,
     *       who in turn manages branch staff.
     * - BRANCH_MANAGER:
     *     - Must be assigned to the branch.
     *     - Can assign ROLE_ASSISTANT and ROLE_EMPLOYEE.
     * - ASSISTANT:
     *     - Must be assigned to the branch.
     *     - Can assign technical/operational staff (ROLE_EMPLOYEE: mechanics, electricians, etc.).
     * - EMPLOYEE:
     *     - Cannot manage or assign other staff.
     *
     * @param targetRoleName role to be assigned to the employee
     * @param branchId branch where the staff is being registered
     * @return true if permitted by hierarchy, false otherwise
     */
    public boolean canManageStaff(String targetRoleName, UUID branchId) {
        if (targetRoleName == null || branchId == null) {
            return false;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof AuthenticatedPrincipal authenticatedPrincipal)) {
            return false;
        }
        if (authenticatedPrincipal.hasRole("ROLE_ADMIN")) {
            return true;
        }
        if (!isAuthorizedForBranch(branchId)) {
            return false;
        }

        String normalizedTarget = targetRoleName.toUpperCase().trim();
        if (!normalizedTarget.startsWith("ROLE_")) {
            normalizedTarget = "ROLE_" + normalizedTarget;
        }

        UUID workshopId = tenantScopeResolver.findWorkshopIdForBranch(branchId);
        int branchCount = workshopId != null ? tenantScopeResolver.countBranchesForWorkshop(workshopId) : 1;
        boolean isSingleBranchWorkshop = branchCount <= 1;

        if (authenticatedPrincipal.hasRole("ROLE_OWNER")) {
            if ("ROLE_BRANCH_MANAGER".equals(normalizedTarget)) {
                return true;
            }
            if (isSingleBranchWorkshop) {
                return "ROLE_ASSISTANT".equals(normalizedTarget) || "ROLE_EMPLOYEE".equals(normalizedTarget);
            }
            return false;
        }

        if (authenticatedPrincipal.hasRole("ROLE_BRANCH_MANAGER")) {
            return "ROLE_ASSISTANT".equals(normalizedTarget) || "ROLE_EMPLOYEE".equals(normalizedTarget);
        }

        if (authenticatedPrincipal.hasRole("ROLE_ASSISTANT")) {
            return "ROLE_EMPLOYEE".equals(normalizedTarget);
        }

        return false;
    }

    public void validateStaffManagement(String targetRoleName, UUID branchId) {
        if (!canManageStaff(targetRoleName, branchId)) {
            throw new AccessDeniedException(String.format(
                    "Unauthorized to manage staff with role '%s' for branch '%s' under workshop hierarchy",
                    targetRoleName, branchId));
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
