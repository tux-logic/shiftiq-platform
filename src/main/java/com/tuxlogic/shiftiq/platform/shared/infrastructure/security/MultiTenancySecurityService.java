package com.tuxlogic.shiftiq.platform.shared.infrastructure.security;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service to validate multi-tenancy access ensuring requested branchId is validated against the authenticated user session.
 *
 * <p>All checks are <b>fail closed</b>: a missing identifier (null) is denied instead of
 * allowed, so omitting {@code branchId}/{@code userId} from a payload can never bypass
 * tenant isolation.</p>
 */
@Service("multiTenancySecurityService")
public class MultiTenancySecurityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiTenancySecurityService.class);

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
            if (authenticatedPrincipal.hasRole("ROLE_ADMIN") || authenticatedPrincipal.hasRole("ROLE_OWNER")) {
                return true;
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
            return authenticatedPrincipal.hasRole("ROLE_ADMIN") || authenticatedPrincipal.hasRole("ROLE_OWNER");
        }
        return false;
    }

    public void validateWorkshopAccess(UUID workshopId) {
        if (!isAuthorizedForWorkshop(workshopId)) {
            throw new AccessDeniedException("Unauthorized access for requested workshop identifier: " + workshopId);
        }
    }
}
