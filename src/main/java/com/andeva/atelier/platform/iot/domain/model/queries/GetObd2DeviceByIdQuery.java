package com.andeva.atelier.platform.iot.domain.model.queries;

import com.andeva.atelier.platform.iot.domain.model.valueobjects.Obd2DeviceId;

/**
 * Query to retrieve an OBD2 device by its unique identifier.
 */
public record GetObd2DeviceByIdQuery(Obd2DeviceId obd2DeviceId) {
}
