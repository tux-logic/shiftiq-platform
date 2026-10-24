package com.tuxlogic.shiftiq.platform.core.domain.model.entities;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopSpecialtyId;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable domain representation of a workshop specialty definition revision snapshot.
 */
public record WorkshopSpecialtyRevision(
        UUID id,
        WorkshopSpecialtyId specialtyId,
        WorkshopId workshopId,
        String name,
        String code,
        String description,
        boolean active,
        String changeType,
        UUID changedBy,
        Instant recordedAt
) {
    public static WorkshopSpecialtyRevision create(
            WorkshopSpecialtyId specialtyId,
            WorkshopId workshopId,
            String name,
            String code,
            String description,
            boolean active,
            String changeType,
            UUID changedBy
    ) {
        return new WorkshopSpecialtyRevision(
                UUID.randomUUID(),
                specialtyId,
                workshopId,
                name,
                code,
                description,
                active,
                changeType,
                changedBy,
                Instant.now()
        );
    }
}
