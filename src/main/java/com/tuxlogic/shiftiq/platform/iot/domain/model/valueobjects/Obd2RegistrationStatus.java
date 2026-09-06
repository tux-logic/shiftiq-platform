package com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects;

/**
 * Value object representing the status of an OBD2 Device Registration.
 */
public record Obd2RegistrationStatus(String value) {
    private static final java.util.Set<String> VALID_STATUSES = java.util.Set.of("ACTIVE", "INACTIVE");

    public Obd2RegistrationStatus {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Obd2 registration status cannot be null or empty");
        }
        if (!VALID_STATUSES.contains(value.toUpperCase())) {
            throw new IllegalArgumentException("Invalid Obd2 registration status: " + value);
        }
    }

    public static final Obd2RegistrationStatus ACTIVE = new Obd2RegistrationStatus("ACTIVE");
    public static final Obd2RegistrationStatus INACTIVE = new Obd2RegistrationStatus("INACTIVE");
}
