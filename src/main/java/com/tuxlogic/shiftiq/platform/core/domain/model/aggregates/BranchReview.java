package com.tuxlogic.shiftiq.platform.core.domain.model.aggregates;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BranchReviewId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class BranchReview extends AbstractDomainAggregateRoot<BranchReview> {

    private BranchReviewId id;
    private BranchId branchId;
    private UUID customerId;
    private UUID appointmentId;
    private int rating;
    private String comment;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private Long version;

    public BranchReview() {
    }

    public BranchReview(
            BranchReviewId id,
            BranchId branchId,
            UUID customerId,
            UUID appointmentId,
            int rating,
            String comment,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt,
            Long version
    ) {
        validateRating(rating);
        this.id = id;
        this.branchId = branchId;
        this.customerId = customerId;
        this.appointmentId = appointmentId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.version = version;
    }

    public BranchReview(BranchId branchId, UUID customerId, UUID appointmentId, int rating, String comment) {
        if (branchId == null) {
            throw new IllegalArgumentException("core.error.branchId.required");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("core.error.customerId.required");
        }
        validateRating(rating);
        this.id = new BranchReviewId(UUID.randomUUID());
        this.branchId = branchId;
        this.customerId = customerId;
        this.appointmentId = appointmentId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    private static void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("core.error.reviewRating.invalid");
        }
    }
}