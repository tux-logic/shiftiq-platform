package com.andeva.atelier.platform.iot.domain.model.commands;

import com.andeva.atelier.platform.iot.domain.model.valueobjects.Obd2DeviceId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

/**
 * Command representing the intent to link an OBD2 device to a vehicle.
 */
public record LinkObd2DeviceToVehicleCommand(
        Obd2DeviceId obd2DeviceId,
        BranchId branchId,
        VehicleId vehicleId
) {
    public LinkObd2DeviceToVehicleCommand {
        if (obd2DeviceId == null) {
            throw new IllegalArgumentException("obd2DeviceId cannot be null");
        }
        if (branchId == null) {
            throw new IllegalArgumentException("branchId cannot be null");
        }
        if (vehicleId == null) {
            throw new IllegalArgumentException("vehicleId cannot be null");
        }
    }
}
