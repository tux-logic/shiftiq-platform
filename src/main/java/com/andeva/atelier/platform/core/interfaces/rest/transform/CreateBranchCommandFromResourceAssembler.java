package com.andeva.atelier.platform.core.interfaces.rest.transform;

import com.andeva.atelier.platform.core.domain.model.commands.CreateBranchCommand;
import com.andeva.atelier.platform.core.domain.model.valueobjects.Phone;
import com.andeva.atelier.platform.core.domain.model.valueobjects.WorkshopId;
import com.andeva.atelier.platform.core.interfaces.rest.resources.CreateBranchResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Address;

public class CreateBranchCommandFromResourceAssembler {
    public static CreateBranchCommand toCommandFromResource(CreateBranchResource resource) {
        return new CreateBranchCommand(
                new WorkshopId(resource.workshopId()),
                resource.code(),
                resource.name(),
                new Address(resource.address()),
                new Phone(resource.phone())
        );
    }
}
