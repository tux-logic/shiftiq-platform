package com.tuxlogic.shiftiq.platform.shared.infrastructure.security;

import com.tuxlogic.shiftiq.platform.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Security evaluation bean exposed to SpEL expressions for checking user permissions and preventing IDOR.
 */
@Service("userSecurityService")
public class UserSecurityService {

    /**
     * Checks whether the given userId matches the ID of the currently authenticated user session.
     *
     * @param userId the UUID of the target user
     * @return true if the authenticated user matches userId, false otherwise
     */
    public boolean isCurrentUser(UUID userId) {
        if (userId == null) {
            return false;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return userId.equals(userDetails.getId());
        }
        return false;
    }
}
