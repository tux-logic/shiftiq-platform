package com.tuxlogic.shiftiq.platform.shared.infrastructure.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service to validate multi-tenancy access ensuring requested branchId is validated against the authenticated user session.
 */
@Service("multiTenancySecurityService")
public class MultiTenancySecurityService {

    public boolean isAuthorizedForBranch(UUID branchId) {
        if (branchId == null) {
            return true;
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

    public void validateBranchAccess(UUID branchId) {
        if (!isAuthorizedForBranch(branchId)) {
            throw new AccessDeniedException("Unauthorized access for requested branch identifier: " + branchId);
        }
    }

    public boolean isAuthorizedForUser(UUID userId) {
        if (userId == null) {
            return true;
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
            return true;
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
