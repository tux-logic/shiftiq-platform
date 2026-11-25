package com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateBranchReviewCommand;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.CreateBranchReviewResource;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import java.util.UUID;

public class CreateBranchReviewCommandFromResourceAssembler {

    public static CreateBranchReviewCommand toCommand(UUID branchId, CreateBranchReviewResource resource) {
        return new CreateBranchReviewCommand(
                new BranchId(branchId),
                resource.customerId(),
                resource.appointmentId(),
                resource.rating(),
                resource.comment()
        );
    }
}