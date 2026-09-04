package com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects;

import java.util.UUID;

public record EmployeeRegistrationId(UUID value) {
    public EmployeeRegistrationId() {
        this(UUID.randomUUID());
    }
}
