package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.SubscriptionPlan;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.SubscriptionPlanId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.SubscriptionPlanRepository;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.assemblers.SubscriptionPlanPersistenceAssembler;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.SubscriptionPlanPersistenceEntity;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories.SubscriptionPlanPersistenceRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Transactional(readOnly = true)
public class SubscriptionPlanRepositoryImpl implements SubscriptionPlanRepository {

    private final SubscriptionPlanPersistenceRepository subscriptionPlanPersistenceRepository;

    public SubscriptionPlanRepositoryImpl(SubscriptionPlanPersistenceRepository subscriptionPlanPersistenceRepository) {
        this.subscriptionPlanPersistenceRepository = subscriptionPlanPersistenceRepository;
    }

    @Override
    @Transactional
    public SubscriptionPlan save(SubscriptionPlan subscriptionPlan) {
        var entity = JpaAdapterUtils.resolveEntity(
                subscriptionPlan.getId() != null ? subscriptionPlan.getId().value() : null,
                subscriptionPlanPersistenceRepository::findById,
                SubscriptionPlanPersistenceEntity::new
        );
        SubscriptionPlanPersistenceAssembler.toEntity(subscriptionPlan, entity);
        return SubscriptionPlanPersistenceAssembler.toDomain(subscriptionPlanPersistenceRepository.save(entity));
    }

    @Override
    public Optional<SubscriptionPlan> findById(SubscriptionPlanId id) {
        return subscriptionPlanPersistenceRepository.findById(id.value()).map(SubscriptionPlanPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<SubscriptionPlan> findByName(String name) {
        return subscriptionPlanPersistenceRepository.findByName(name).map(SubscriptionPlanPersistenceAssembler::toDomain);
    }

    @Override
    public List<SubscriptionPlan> findAll() {
        return subscriptionPlanPersistenceRepository.findAll().stream()
                .map(SubscriptionPlanPersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }
}
