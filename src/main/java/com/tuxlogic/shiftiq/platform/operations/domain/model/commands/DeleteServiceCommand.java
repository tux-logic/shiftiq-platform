package com.tuxlogic.shiftiq.platform.operations.domain.model.commands;

import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.ServiceId;

/**
 * Command to delete a service by its ID.
 * @param serviceId the unique identifier of the service to be deleted
 * @author Joel Huamani Estefanero
 */
public record DeleteServiceCommand(ServiceId serviceId) {
    public DeleteServiceCommand {
        if (serviceId == null) throw new IllegalArgumentException("operations.error.command.serviceId.required");
    }
}
