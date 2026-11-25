package com.tuxlogic.shiftiq.platform.shared.infrastructure.security;

import java.util.UUID;

/**
 * Resolves the customer profile owned by an authenticated user.
 *
 * <p>Kept in the shared kernel so appointment and fleet authorization can check
 * customer self-access without depending on the Core bounded context.</p>
 */
public interface CustomerScopeResolver {

    /**
     * @param userId authenticated IAM user id
     * @return the customer profile id linked to that user, or {@code null} when none exists
     */
    UUID findCustomerIdForUser(UUID userId);
}
