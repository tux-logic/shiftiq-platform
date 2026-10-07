package com.tuxlogic.shiftiq.platform.analytics.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.analytics.infrastructure.persistence.jpa.entities.BranchAnalyticsPendingDeltaPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaBranchAnalyticsPendingDeltaRepository
        extends JpaRepository<BranchAnalyticsPendingDeltaPersistenceEntity, UUID> {
}
