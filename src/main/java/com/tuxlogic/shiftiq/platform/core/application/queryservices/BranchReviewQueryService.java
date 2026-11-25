package com.tuxlogic.shiftiq.platform.core.application.queryservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.BranchReview;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetBranchReviewsByBranchIdQuery;

import java.util.List;

public interface BranchReviewQueryService {
    List<BranchReview> handle(GetBranchReviewsByBranchIdQuery query);
}