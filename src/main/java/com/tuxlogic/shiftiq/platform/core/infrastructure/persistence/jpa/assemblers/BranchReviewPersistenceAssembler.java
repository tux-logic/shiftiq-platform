package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.assemblers;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.BranchReview;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BranchReviewId;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.BranchReviewPersistenceEntity;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

public class BranchReviewPersistenceAssembler {

    public static BranchReview toDomain(BranchReviewPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new BranchReview(
                new BranchReviewId(entity.getId()),
                new BranchId(entity.getBranchId()),
                entity.getCustomerId(),
                entity.getAppointmentId(),
                entity.getRating(),
                entity.getComment(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getVersion()
        );
    }

    public static void toEntity(BranchReview domain, BranchReviewPersistenceEntity entity) {
        if (domain.getVersion() != null) {
            entity.setId(domain.getId() != null ? domain.getId().value() : null);
            entity.setCreatedAt(domain.getCreatedAt());
            entity.setUpdatedAt(domain.getUpdatedAt());
            entity.setVersion(domain.getVersion());
        }
        if (domain.getBranchId() != null) {
            entity.setBranchId(domain.getBranchId().value());
        }
        entity.setCustomerId(domain.getCustomerId());
        entity.setAppointmentId(domain.getAppointmentId());
        entity.setRating(domain.getRating());
        entity.setComment(domain.getComment());
        entity.setDeletedAt(domain.getDeletedAt());
    }
}