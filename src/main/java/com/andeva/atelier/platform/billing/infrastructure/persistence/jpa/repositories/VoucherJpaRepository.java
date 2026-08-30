package com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.entities.VoucherPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface VoucherJpaRepository extends JpaRepository<VoucherPersistenceEntity, UUID> {
    
    @Query("SELECT v FROM VoucherPersistenceEntity v WHERE v.quoteId IN (SELECT q.id FROM QuotePersistenceEntity q WHERE q.branchId = :branchId)")
    List<VoucherPersistenceEntity> findByBranchId(@Param("branchId") com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId branchId);
}
