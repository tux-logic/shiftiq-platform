package com.tuxlogic.shiftiq.platform.core.domain.model.commands;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import java.util.UUID;

public record CreateBranchReviewCommand(
        BranchId branchId,
        UUID customerId,
        UUID appointmentId,
        int rating,
        String comment
) {
    public CreateBranchReviewCommand {
        if (branchId == null) {
            throw new IllegalArgumentException("core.error.branchId.required");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("core.error.customerId.required");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("core.error.reviewRating.invalid");
        }
    }
}