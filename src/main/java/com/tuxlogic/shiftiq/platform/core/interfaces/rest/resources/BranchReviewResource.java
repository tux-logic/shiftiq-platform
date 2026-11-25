package com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record BranchReviewResource(
        UUID id,
        UUID branchId,
        UUID customerId,
        UUID appointmentId,
        int rating,
        String comment,
        Instant createdAt
) {
}