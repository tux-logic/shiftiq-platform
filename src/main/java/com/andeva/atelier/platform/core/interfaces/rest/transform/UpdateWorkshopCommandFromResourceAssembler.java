package com.andeva.atelier.platform.core.interfaces.rest.transform;

import com.andeva.atelier.platform.core.domain.model.commands.UpdateWorkshopCommand;
import com.andeva.atelier.platform.core.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.core.domain.model.valueobjects.WorkshopId;
import com.andeva.atelier.platform.core.interfaces.rest.resources.UpdateWorkshopResource;

import java.util.UUID;

public class UpdateWorkshopCommandFromResourceAssembler {
    public static UpdateWorkshopCommand toCommandFromResource(UUID id, UpdateWorkshopResource resource) {
        return new UpdateWorkshopCommand(
                new WorkshopId(id),
                resource.businessName(),
                resource.brandName(),
                new TaxId(resource.taxId()),
                resource.mileageIntervalConfig()
        );
    }
}
