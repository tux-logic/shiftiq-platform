package com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateWorkshopSpecialtyCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.CreateWorkshopSpecialtyResource;

import java.util.UUID;

public class CreateWorkshopSpecialtyCommandFromResourceAssembler {

    public static CreateWorkshopSpecialtyCommand toCommand(UUID workshopId, CreateWorkshopSpecialtyResource resource) {
        return new CreateWorkshopSpecialtyCommand(
                new WorkshopId(workshopId),
                resource.name(),
                resource.code(),
                resource.description()
        );
    }
}
