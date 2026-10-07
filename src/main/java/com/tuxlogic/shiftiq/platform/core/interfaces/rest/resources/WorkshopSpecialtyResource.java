package com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record WorkshopSpecialtyResource(
        UUID id,
        UUID workshopId,
        String name,
        String code,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
