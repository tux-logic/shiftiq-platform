package com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateWorkshopSpecialtyCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopSpecialtyId;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.UpdateWorkshopSpecialtyResource;

import java.util.UUID;

public class UpdateWorkshopSpecialtyCommandFromResourceAssembler {

    public static UpdateWorkshopSpecialtyCommand toCommand(UUID specialtyId, UpdateWorkshopSpecialtyResource resource) {
        return new UpdateWorkshopSpecialtyCommand(
                new WorkshopSpecialtyId(specialtyId),
                resource.name(),
                resource.description()
        );
    }
}
