package com.tuxlogic.shiftiq.platform.shared.infrastructure.security;

import java.util.Set;
import java.util.UUID;

/**
 * Abstraction of the authenticated caller exposed by the shared kernel.
 *
 * <p>The shared kernel must not depend on a concrete bounded context, so the
 * security components it owns (auditing, multi-tenancy, IDOR checks) program
 * against this interface. The IAM context provides the implementation that
 * backs it with the authenticated user.</p>
 */
public interface AuthenticatedPrincipal {

    /**
     * @return the identifier of the authenticated user
     */
    UUID getId();

    /**
     * @param role the authority to look for, e.g. {@code ROLE_ADMIN}
     * @return true when the principal holds the given authority
     */
    boolean hasRole(String role);

    /**
     * @param permission the permission code to look for, e.g. {@code INVENTORY_MANAGE}
     * @return true when the principal holds the given permission authority
     */
    default boolean hasPermission(String permission) {
        return permission != null && hasRole(permission);
    }

    /**
     * @return the permission codes held by the principal, never null
     */
    default Set<String> getPermissions() {
        return Set.of();
    }

    /**
     * @param branchId the branch to look for
     * @return true when the principal is a member of the given branch
     */
    boolean hasBranch(UUID branchId);

    /**
     * @return the identifiers of the branches the principal is a member of, never null
     */
    Set<UUID> getBranchIds();
}
