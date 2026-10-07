package com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects;

import java.util.UUID;

public record WorkshopSpecialtyId(UUID value) {
    public WorkshopSpecialtyId {
        if (value == null) {
            throw new IllegalArgumentException("WorkshopSpecialtyId cannot be null");
        }
    }
}
