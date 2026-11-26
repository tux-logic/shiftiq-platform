package com.tuxlogic.shiftiq.platform.core.domain.repositories;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.BranchReview;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BranchReviewId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import java.util.List;
import java.util.Optional;

public interface BranchReviewRepository {
    BranchReview save(BranchReview review);
    Optional<BranchReview> findById(BranchReviewId id);
    List<BranchReview> findByBranchId(BranchId branchId);
    Double averageRatingByBranchId(BranchId branchId);
    int countByBranchId(BranchId branchId);
    boolean existsByAppointmentId(java.util.UUID appointmentId);
}