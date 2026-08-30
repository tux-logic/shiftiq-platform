package com.tuxlogic.shiftiq.platform.iot.domain.model.commands;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;

/**
 * Command representing the intent to delete (unregister) a Vehicle.
 */
public record DeleteVehicleCommand(VehicleId vehicleId) {
    public DeleteVehicleCommand {
        if (vehicleId == null) {
            throw new IllegalArgumentException("vehicleId cannot be null");
        }
    }
}
