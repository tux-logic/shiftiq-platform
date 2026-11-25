package com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.BranchReview;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.BranchReviewResource;

public class BranchReviewResourceFromAggregateAssembler {

    public static BranchReviewResource toResource(BranchReview review) {
        return new BranchReviewResource(
                review.getId().value(),
                review.getBranchId().value(),
                review.getCustomerId(),
                review.getAppointmentId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}