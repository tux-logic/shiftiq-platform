package com.tuxlogic.shiftiq.platform.iot.domain.model.queries;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;

/**
 * Query to retrieve a Vehicle by its unique identifier.
 */
public record GetVehicleByIdQuery(VehicleId vehicleId) {
}
