package com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateBranchCommand;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Phone;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.UpdateBranchResource;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Address;

import java.util.UUID;

public final class UpdateBranchCommandFromResourceAssembler {

    private UpdateBranchCommandFromResourceAssembler() {}

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

