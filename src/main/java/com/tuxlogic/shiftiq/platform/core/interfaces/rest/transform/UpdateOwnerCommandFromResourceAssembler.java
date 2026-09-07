package com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateOwnerCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Document;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.PersonName;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Phone;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.OwnerId;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.UpdateOwnerResource;

import java.util.UUID;

public final class UpdateOwnerCommandFromResourceAssembler {

    private UpdateOwnerCommandFromResourceAssembler() {}

    public static UpdateOwnerCommand toCommandFromResource(UUID ownerId, UpdateOwnerResource resource) {
        return new UpdateOwnerCommand(
                new OwnerId(ownerId),
                new PersonName(resource.firstName(), resource.lastName()),
                new Document(resource.documentType(), resource.documentNumber()),
                new Phone(resource.phone())
        );
    }
}
