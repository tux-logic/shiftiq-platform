package com.andeva.atelier.platform.core.interfaces.rest.transform;

import com.andeva.atelier.platform.core.domain.model.commands.UpdateOwnerCommand;
import com.andeva.atelier.platform.core.domain.model.valueobjects.Document;
import com.andeva.atelier.platform.core.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.core.domain.model.valueobjects.Phone;
import com.andeva.atelier.platform.core.domain.model.valueobjects.OwnerId;
import com.andeva.atelier.platform.core.interfaces.rest.resources.UpdateOwnerResource;

import java.util.UUID;

public class UpdateOwnerCommandFromResourceAssembler {
    public static UpdateOwnerCommand toCommandFromResource(UUID ownerId, UpdateOwnerResource resource) {
        return new UpdateOwnerCommand(
                new OwnerId(ownerId),
                new PersonName(resource.firstName(), resource.lastName()),
                new Document(resource.documentType(), resource.documentNumber()),
                new Phone(resource.phone())
        );
    }
}
