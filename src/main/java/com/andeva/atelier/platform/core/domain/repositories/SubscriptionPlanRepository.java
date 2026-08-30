package com.andeva.atelier.platform.core.domain.repositories;

import com.andeva.atelier.platform.core.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.core.domain.model.valueobjects.SubscriptionPlanId;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository {
    SubscriptionPlan save(SubscriptionPlan subscriptionPlan);
    Optional<SubscriptionPlan> findById(SubscriptionPlanId id);
    Optional<SubscriptionPlan> findByName(String name);
    List<SubscriptionPlan> findAll();
}
