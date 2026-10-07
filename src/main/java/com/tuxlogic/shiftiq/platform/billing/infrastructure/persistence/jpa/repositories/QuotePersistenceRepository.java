package com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.billing.infrastructure.persistence.jpa.entities.QuotePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuotePersistenceRepository extends JpaRepository<QuotePersistenceEntity, UUID> {
    List<QuotePersistenceEntity> findAllByBranchId(com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId branchId);
    boolean existsByWorkOrderId(UUID workOrderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM QuotePersistenceEntity q WHERE q.id = :id")
    Optional<QuotePersistenceEntity> findByIdForUpdate(@Param("id") UUID id);
}
