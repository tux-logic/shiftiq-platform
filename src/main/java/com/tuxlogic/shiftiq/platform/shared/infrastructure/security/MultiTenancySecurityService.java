package com.tuxlogic.shiftiq.platform.shared.infrastructure.security;

import com.tuxlogic.shiftiq.platform.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
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
        if (principal instanceof UserDetailsImpl userDetails) {
            if (userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_OWNER"))) {
                return true;
            }
            return userDetails.getBranchIds() != null && userDetails.getBranchIds().contains(branchId);
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
        if (principal instanceof UserDetailsImpl userDetails) {
            if (userDetails.getId().equals(userId)) {
                return true;
            }
            return userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_OWNER"));
        }
        return false;
    }

    public void validateUserAccess(UUID userId) {
        if (!isAuthorizedForUser(userId)) {
            throw new AccessDeniedException("Unauthorized access for requested user identifier: " + userId);
        }
    }
}
