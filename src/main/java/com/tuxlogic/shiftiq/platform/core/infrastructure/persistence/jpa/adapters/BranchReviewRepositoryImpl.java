package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.BranchReview;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BranchReviewId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.BranchReviewRepository;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.assemblers.BranchReviewPersistenceAssembler;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.BranchReviewPersistenceEntity;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories.BranchReviewPersistenceRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class BranchReviewRepositoryImpl implements BranchReviewRepository {

    private final BranchReviewPersistenceRepository repository;

    public BranchReviewRepositoryImpl(BranchReviewPersistenceRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public BranchReview save(BranchReview review) {
        var entity = JpaAdapterUtils.resolveEntity(
                review.getId() != null ? review.getId().value() : null,
                repository::findById,
                BranchReviewPersistenceEntity::new
        );
        BranchReviewPersistenceAssembler.toEntity(review, entity);
        return BranchReviewPersistenceAssembler.toDomain(repository.save(entity));
    }

    @Override
    public Optional<BranchReview> findById(BranchReviewId id) {
        return repository.findById(id.value()).map(BranchReviewPersistenceAssembler::toDomain);
    }

    @Override
    public List<BranchReview> findByBranchId(BranchId branchId) {
        return repository.findByBranchIdOrderByCreatedAtDesc(branchId.value()).stream()
                .map(BranchReviewPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public Double averageRatingByBranchId(BranchId branchId) {
        return repository.averageRatingByBranchId(branchId.value());
    }

    @Override
    public int countByBranchId(BranchId branchId) {
        return repository.countByBranchId(branchId.value());
    }

    @Override
    public boolean existsByAppointmentId(UUID appointmentId) {
        return repository.existsByAppointmentId(appointmentId);
    }
}