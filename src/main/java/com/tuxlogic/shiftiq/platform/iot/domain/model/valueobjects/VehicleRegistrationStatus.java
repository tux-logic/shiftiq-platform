package com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects;

/**
 * Value object representing the status of a Vehicle Registration.
 */
public record VehicleRegistrationStatus(String value) {
    private static final java.util.Set<String> VALID_STATUSES = java.util.Set.of("ACTIVE", "PREVIOUS");

    public VehicleRegistrationStatus {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Vehicle registration status cannot be null or empty");
        }
        if (!VALID_STATUSES.contains(value.toUpperCase())) {
            throw new IllegalArgumentException("Invalid vehicle registration status: " + value);
        }
    }

    public static final VehicleRegistrationStatus ACTIVE = new VehicleRegistrationStatus("ACTIVE");
    public static final VehicleRegistrationStatus PREVIOUS = new VehicleRegistrationStatus("PREVIOUS");
}