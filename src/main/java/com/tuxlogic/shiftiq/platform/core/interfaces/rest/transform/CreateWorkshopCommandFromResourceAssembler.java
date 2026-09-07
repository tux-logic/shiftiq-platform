package com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateWorkshopCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.OwnerId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.TaxId;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.CreateWorkshopResource;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.MileageIntervalConfig;

public final class CreateWorkshopCommandFromResourceAssembler {

    private CreateWorkshopCommandFromResourceAssembler() {}

    public static CreateWorkshopCommand toCommandFromResource(CreateWorkshopResource resource) {
        return new CreateWorkshopCommand(
                new OwnerId(resource.ownerId()),
                resource.businessName(),
                resource.brandName(),
                new TaxId(resource.taxId()),
                new MileageIntervalConfig(resource.mileageIntervalConfig())
        );
    }
}
