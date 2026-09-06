package com.tuxlogic.shiftiq.platform.iot.domain.model.queries;

import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.Obd2DeviceId;

/**
 * Query to retrieve the complete history of telemetry snapshots for a specific OBD2 device.
 */
public record GetTelemetrySnapshotHistoryQuery(Obd2DeviceId obd2DeviceId, int page, int size) {
    public GetTelemetrySnapshotHistoryQuery(Obd2DeviceId obd2DeviceId) {
        this(obd2DeviceId, 0, 20);
    }
}
