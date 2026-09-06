package com.tuxlogic.shiftiq.platform.iot.domain.model.queries;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;

/**
 * Domain query representing the request to retrieve telemetry snapshot history for a vehicle
 * starting from the start of its active registration/linking.
 */
public record GetVehicleTelemetrySnapshotHistoryQuery(
        VehicleId vehicleId,
        int page,
        int size
) {
    public GetVehicleTelemetrySnapshotHistoryQuery(VehicleId vehicleId) {
        this(vehicleId, 0, 20);
    }

    public GetVehicleTelemetrySnapshotHistoryQuery {
        if (vehicleId == null) {
            throw new IllegalArgumentException("vehicleId cannot be null");
        }
    }
}
