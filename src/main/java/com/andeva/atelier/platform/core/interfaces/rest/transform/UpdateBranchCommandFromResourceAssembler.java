package com.andeva.atelier.platform.core.interfaces.rest.transform;

import com.andeva.atelier.platform.core.domain.model.commands.UpdateBranchCommand;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.core.domain.model.valueobjects.Phone;
import com.andeva.atelier.platform.core.interfaces.rest.resources.UpdateBranchResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Address;

import java.util.UUID;

public class UpdateBranchCommandFromResourceAssembler {
    public static UpdateBranchCommand toCommandFromResource(UUID id, UpdateBranchResource resource) {
        return new UpdateBranchCommand(
                new BranchId(id),
                resource.code(),
                resource.name(),
                new Address(resource.address()),
                new Phone(resource.phone())
        );
    }
}

