package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.BranchBayPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BranchBayPersistenceRepository extends JpaRepository<BranchBayPersistenceEntity, UUID> {
    List<BranchBayPersistenceEntity> findAllByBranchId(UUID branchId);
    List<BranchBayPersistenceEntity> findAllByBranchIdAndActiveTrue(UUID branchId);
    boolean existsByBranchIdAndCode(UUID branchId, String code);
    boolean existsByIdAndBranchId(UUID id, UUID branchId);
    int countByBranchIdAndActiveTrue(UUID branchId);
}