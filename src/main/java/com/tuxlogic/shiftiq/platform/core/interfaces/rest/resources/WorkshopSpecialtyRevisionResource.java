package com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record WorkshopSpecialtyRevisionResource(
        UUID id,
        UUID specialtyId,
        UUID workshopId,
        String name,
        String code,
        String description,
        boolean active,
        String changeType,
        UUID changedBy,
        Instant recordedAt
) {
}
