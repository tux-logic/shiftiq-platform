package com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects;

import java.time.Instant;

/**
 * Value object holding the active registration context (OBD2 device registration ID
 * and driver registration start timestamp) for a vehicle.
 */
public record ActiveRegistrationContext(
        Obd2DeviceRegistrationId obd2DeviceRegistrationId,
        Instant startTimestamp
) {
    public ActiveRegistrationContext {
        if (obd2DeviceRegistrationId == null) {
            throw new IllegalArgumentException("obd2DeviceRegistrationId cannot be null");
        }
        if (startTimestamp == null) {
            throw new IllegalArgumentException("startTimestamp cannot be null");
        }
    }
}
