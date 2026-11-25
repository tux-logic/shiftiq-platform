package com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateBranchBayCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BayType;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BranchBayId;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.UpdateBranchBayResource;

import java.util.UUID;

public class UpdateBranchBayCommandFromResourceAssembler {

    public static UpdateBranchBayCommand toCommand(UUID bayId, UpdateBranchBayResource resource) {
        return new UpdateBranchBayCommand(
                new BranchBayId(bayId),
                resource.name(),
                BayType.valueOf(resource.type().trim().toUpperCase())
        );
    }
}