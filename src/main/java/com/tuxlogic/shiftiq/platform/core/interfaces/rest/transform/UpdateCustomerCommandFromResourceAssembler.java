package com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateCustomerCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Document;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.PersonName;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Phone;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.UpdateCustomerResource;

import java.util.UUID;

public class UpdateCustomerCommandFromResourceAssembler {
    public static UpdateCustomerCommand toCommandFromResource(UUID customerId, UpdateCustomerResource resource) {
        return new UpdateCustomerCommand(
                new CustomerId(customerId),
                new PersonName(resource.firstName(), resource.lastName()),
                resource.businessName(),
                new Document(resource.documentType(), resource.documentNumber()),
                new Phone(resource.phone())
        );
    }
}
