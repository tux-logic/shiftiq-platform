package com.tuxlogic.shiftiq.platform.shared.infrastructure.security;

import java.util.List;
import java.util.UUID;

/**
 * Resolves the customer profile and workshop affiliations owned by an authenticated user.
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

    /**
     * @param customerId customer profile id
     * @return distinct active workshop ids where the customer holds an ACTIVE registration
     */
    default List<UUID> findCustomerWorkshopIds(UUID customerId) {
        return List.of();
    }

    /**
     * Checks whether the customer has an ACTIVE registration in the target branch
     * or in another active branch belonging to the same workshop.
     */
    default boolean isCustomerAssociatedWithBranchWorkshop(UUID customerId, UUID branchId) {
        return false;
    }
}
