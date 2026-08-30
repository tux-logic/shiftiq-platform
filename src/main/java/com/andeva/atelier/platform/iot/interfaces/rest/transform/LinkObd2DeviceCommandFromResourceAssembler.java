package com.andeva.atelier.platform.iot.interfaces.rest.transform;

import com.andeva.atelier.platform.iot.domain.model.commands.LinkObd2DeviceToVehicleCommand;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.Obd2DeviceId;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.LinkObd2DeviceResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

/**
 * Assembler to translate LinkObd2DeviceResource to LinkObd2DeviceToVehicleCommand.
 */
public class LinkObd2DeviceCommandFromResourceAssembler {
    public static LinkObd2DeviceToVehicleCommand toCommandFromResource(LinkObd2DeviceResource resource) {
        return new LinkObd2DeviceToVehicleCommand(
                new Obd2DeviceId(resource.obd2DeviceId()),
                new BranchId(resource.branchId()),
                new VehicleId(resource.vehicleId())
        );
    }
}
