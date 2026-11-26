package com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateBranchBayCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BayType;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.CreateBranchBayResource;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import java.util.UUID;

public class CreateBranchBayCommandFromResourceAssembler {

    public static CreateBranchBayCommand toCommand(UUID branchId, CreateBranchBayResource resource) {
        return new CreateBranchBayCommand(
                new BranchId(branchId),
                resource.code(),
                resource.name(),
                BayType.valueOf(resource.type().trim().toUpperCase())
        );
    }
}