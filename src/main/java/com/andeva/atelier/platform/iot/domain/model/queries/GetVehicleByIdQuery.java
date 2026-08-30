package com.andeva.atelier.platform.iot.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

/**
 * Query to retrieve a Vehicle by its unique identifier.
 */
public record GetVehicleByIdQuery(VehicleId vehicleId) {
}
