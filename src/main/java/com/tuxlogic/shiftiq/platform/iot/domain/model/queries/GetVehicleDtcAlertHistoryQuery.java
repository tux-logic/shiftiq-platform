package com.tuxlogic.shiftiq.platform.iot.domain.model.queries;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;

/**
 * Domain query representing the request to retrieve historical DTC alerts for a vehicle
 * starting from the start of its active driver registration.
 */
public record GetVehicleDtcAlertHistoryQuery(
        VehicleId vehicleId,
        int page,
        int size
) {
    public GetVehicleDtcAlertHistoryQuery(VehicleId vehicleId) {
        this(vehicleId, 0, 20);
    }

    public GetVehicleDtcAlertHistoryQuery {
        if (vehicleId == null) {
            throw new IllegalArgumentException("vehicleId cannot be null");
        }
    }
}
