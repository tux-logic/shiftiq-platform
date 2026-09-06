package com.tuxlogic.shiftiq.platform.iot.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.iot.domain.model.commands.RegisterVehicleCommand;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.resources.RegisterVehicleResource;

import java.util.UUID;

/**
 * Assembler to translate RegisterVehicleResource and userId to RegisterVehicleCommand.
 */
public class RegisterVehicleCommandFromResourceAssembler {

    private RegisterVehicleCommandFromResourceAssembler() {}

    public static RegisterVehicleCommand toCommandFromResource(UUID userId, RegisterVehicleResource resource) {
        return new RegisterVehicleCommand(
                userId,
                resource.plateNumber(),
                resource.brand(),
                resource.model(),
                resource.year(),
                resource.vin()
        );
    }
}