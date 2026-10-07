package com.tuxlogic.shiftiq.platform.shared.infrastructure.security;

import java.util.Set;
import java.util.UUID;

/**
 * Resolves the tenancy scope (workshops and branches) owned by a user.
 *
 * <p>The shared kernel must not depend on concrete bounded contexts, so it declares this
 * port and the core context provides the implementation backed by the owner, workshop and
 * branch aggregates. Implementations must fail closed: a missing link in the chain
 * (user without owner profile, owner without workshops, workshop without branches) yields
 * an empty scope.</p>
 */
public interface TenantScopeResolver {

    /**
     * @param userId identifier of the authenticated user
     * @return the workshop ids owned by the user, empty when the user owns no workshop
     */
    Set<UUID> findWorkshopIdsForUser(UUID userId);

    /**
     * @param userId identifier of the authenticated user
     * @return the branch ids belonging to the workshops owned by the user, empty when none
     */
    Set<UUID> findBranchIdsForUser(UUID userId);

    /**
     * @param userId identifier of the authenticated user
     * @return the owner profile identifier linked to the user, or {@code null} when the
     *         user has no owner profile
     */
    UUID findOwnerProfileIdForUser(UUID userId);

    /**
     * @return the ids of every non-deleted branch of the platform (including branches
     *         without an owner), used for unrestricted aggregation scopes
     */
    Set<UUID> findAllBranchIds();

    /**
     * @param branchId identifier of the branch to check
     * @return {@code true} only when the branch exists and is not soft deleted
     */
    boolean branchExists(UUID branchId);
}
