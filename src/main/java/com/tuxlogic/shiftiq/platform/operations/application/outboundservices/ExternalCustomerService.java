package com.tuxlogic.shiftiq.platform.operations.application.outboundservices;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound ACL service interface for fetching customer details from the Core context.
 */
public interface ExternalCustomerService {

    /**
     * Fetches the display name (full name or business name) of a customer.
     *
     * @param customerId The customer profile identifier.
     * @return An Optional containing the customer's display name if found.
     */
    Optional<String> fetchCustomerName(UUID customerId);

    /**
     * Resolves the customer profile identifier associated with an IAM user ID.
     *
     * @param userId The IAM user identifier.
     * @return An Optional containing the customer profile UUID if found.
     */
    Optional<UUID> fetchCustomerIdByUserId(UUID userId);
}
