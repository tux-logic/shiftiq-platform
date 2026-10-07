package com.tuxlogic.shiftiq.platform.shared.infrastructure.security;

import java.util.Set;
import java.util.UUID;

/**
 * Read-only port resolving the branch memberships stored for an account, so callers
 * scoped to branches (branch managers, assistants) can be limited to the users that
 * share at least one of their branches instead of getting a global bypass.
 *
 * <p>Implemented by the IAM context, which owns the user account aggregate.</p>
 */
public interface AccountBranchScopeResolver {

    /**
     * @param accountId identifier of the user account
     * @return the branch ids the account belongs to, empty when the account has none
     */
    Set<UUID> findBranchIdsForAccount(UUID accountId);
}
