package com.tuxlogic.shiftiq.platform.core.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.core.application.queryservices.BranchReviewQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.BranchReview;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetBranchReviewsByBranchIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.BranchReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BranchReviewQueryServiceImpl implements BranchReviewQueryService {

    private final BranchReviewRepository reviewRepository;

    public BranchReviewQueryServiceImpl(BranchReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public List<BranchReview> handle(GetBranchReviewsByBranchIdQuery query) {
        return reviewRepository.findByBranchId(query.branchId());
    }
}