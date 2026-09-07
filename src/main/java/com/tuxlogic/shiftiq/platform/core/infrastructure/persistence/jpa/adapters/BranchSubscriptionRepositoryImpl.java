package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.BranchSubscription;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BranchSubscriptionId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.SubscriptionStatus;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.BranchSubscriptionRepository;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.assemblers.BranchSubscriptionPersistenceAssembler;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.BranchSubscriptionPersistenceEntity;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories.BranchSubscriptionPersistenceRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class BranchSubscriptionRepositoryImpl implements BranchSubscriptionRepository {

    private final BranchSubscriptionPersistenceRepository branchSubscriptionPersistenceRepository;

    public BranchSubscriptionRepositoryImpl(BranchSubscriptionPersistenceRepository branchSubscriptionPersistenceRepository) {
        this.branchSubscriptionPersistenceRepository = branchSubscriptionPersistenceRepository;
    }

    @Override
    @Transactional
    public BranchSubscription save(BranchSubscription branchSubscription) {
        var entity = JpaAdapterUtils.resolveEntity(
                branchSubscription.getId() != null ? branchSubscription.getId().value() : null,
                branchSubscriptionPersistenceRepository::findById,
                BranchSubscriptionPersistenceEntity::new
        );
        BranchSubscriptionPersistenceAssembler.toEntity(branchSubscription, entity);
        return BranchSubscriptionPersistenceAssembler.toDomain(branchSubscriptionPersistenceRepository.save(entity));
    }

    @Override
    public Optional<BranchSubscription> findById(BranchSubscriptionId id) {
        return branchSubscriptionPersistenceRepository.findById(id.value()).map(BranchSubscriptionPersistenceAssembler::toDomain);
    }

    @Override
    public List<BranchSubscription> findAllByBranchId(BranchId branchId) {
        return branchSubscriptionPersistenceRepository.findAllByBranchId(branchId.value()).stream()
                .map(BranchSubscriptionPersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BranchSubscription> findActiveByBranchId(BranchId branchId) {
        return branchSubscriptionPersistenceRepository.findByBranchIdAndStatus(branchId.value(), SubscriptionStatus.ACTIVE)
                .map(BranchSubscriptionPersistenceAssembler::toDomain);
    }
}

