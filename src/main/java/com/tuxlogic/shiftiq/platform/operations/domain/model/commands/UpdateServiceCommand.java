package com.tuxlogic.shiftiq.platform.operations.domain.model.commands;

import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.ServiceId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;

/**
 * Command to update an existing service with new name and price.
 * @param serviceId the unique identifier of the service to be updated
 * @param name the new name of the service
 * @param price the new price of the service
 */
public record UpdateServiceCommand(ServiceId serviceId, String name, Money price) {
     public UpdateServiceCommand {
         if (serviceId == null) throw new IllegalArgumentException("operations.error.command.serviceId.required");
         if (name == null || name.isBlank()) throw new IllegalArgumentException("core.error.name.required");
         if (price == null) throw new IllegalArgumentException("operations.error.command.price.required");
     }
}
