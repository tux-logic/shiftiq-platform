package com.andeva.atelier.platform.iot.interfaces.rest.transform;

import com.andeva.atelier.platform.iot.domain.model.commands.CreateObd2DeviceCommand;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.CreateObd2DeviceResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;

/**
 * Assembler to translate CreateObd2DeviceResource to CreateObd2DeviceCommand.
 */
public class CreateObd2DeviceCommandFromResourceAssembler {
    public static CreateObd2DeviceCommand toCommandFromResource(CreateObd2DeviceResource resource) {
        return new CreateObd2DeviceCommand(
                new BranchId(resource.branchId()),
                resource.macAddress()
        );
    }
}
