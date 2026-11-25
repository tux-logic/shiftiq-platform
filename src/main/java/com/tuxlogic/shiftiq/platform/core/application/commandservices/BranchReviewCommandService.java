package com.tuxlogic.shiftiq.platform.core.application.commandservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.BranchReview;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateBranchReviewCommand;

import java.util.Optional;

public interface BranchReviewCommandService {
    Optional<BranchReview> handle(CreateBranchReviewCommand command);
}