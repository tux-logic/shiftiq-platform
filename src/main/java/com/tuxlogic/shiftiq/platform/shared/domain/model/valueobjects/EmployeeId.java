package com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects;

import java.util.UUID;

/**
 * Shared Value Object representing an Employee identifier across bounded contexts.
 * Moved from {@code core.domain.model.valueobjects} to {@code shared} to avoid
 * cross-BC coupling with the Fleet bounded context.
 */
public record EmployeeId(UUID value) {

    public EmployeeId {
        if (value == null) {
            throw new IllegalArgumentException("shared.error.employeeId.required");
        }
    }

    public static EmployeeId of(UUID value) {
        return new EmployeeId(value);
    }

    public static EmployeeId generate() {
        return new EmployeeId(UUID.randomUUID());
    }
}
