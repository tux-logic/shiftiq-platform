package com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects;

import java.util.UUID;

public record OwnerId(UUID value) {
    public OwnerId {
        if (value == null) {
            throw new IllegalArgumentException("core.error.ownerId.required");
        }
    }
}
