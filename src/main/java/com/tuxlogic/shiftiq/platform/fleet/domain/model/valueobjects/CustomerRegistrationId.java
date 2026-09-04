package com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects;

import java.util.UUID;

public record CustomerRegistrationId(UUID value) {
    public CustomerRegistrationId() {
        this(UUID.randomUUID());
    }
}
