package com.andeva.atelier.platform.core.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.core.domain.model.valueobjects.SubscriptionStatus;
import com.andeva.atelier.platform.core.infrastructure.persistence.jpa.entities.BranchSubscriptionPersistenceEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BranchSubscriptionPersistenceRepository extends JpaRepository<BranchSubscriptionPersistenceEntity, UUID> {
    List<BranchSubscriptionPersistenceEntity> findAllByBranchId(UUID branchId);
    Optional<BranchSubscriptionPersistenceEntity> findByBranchIdAndStatus(UUID branchId, SubscriptionStatus status);
}
