package com.andeva.atelier.platform.fleet.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record CustomerRegistrationResource(
        UUID id,
        UUID branchId,
        UUID customerId,
        String status,
        Instant createdAt,
        Instant deletedAt
) {
}

