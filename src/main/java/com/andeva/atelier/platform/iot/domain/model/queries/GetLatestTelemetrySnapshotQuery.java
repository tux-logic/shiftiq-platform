package com.andeva.atelier.platform.iot.domain.model.queries;

import com.andeva.atelier.platform.iot.domain.model.valueobjects.Obd2DeviceId;

/**
 * Query to retrieve the latest telemetry snapshot for a specific OBD2 device.
 */
public record GetLatestTelemetrySnapshotQuery(Obd2DeviceId obd2DeviceId) {
}
