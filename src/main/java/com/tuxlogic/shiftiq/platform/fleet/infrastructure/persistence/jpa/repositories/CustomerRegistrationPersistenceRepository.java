package com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.entities.CustomerRegistrationPersistenceEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRegistrationPersistenceRepository extends JpaRepository<CustomerRegistrationPersistenceEntity, UUID> {
    java.util.List<CustomerRegistrationPersistenceEntity> findByCustomerId(UUID customerId);
    Page<CustomerRegistrationPersistenceEntity> findByBranchIdAndStatus(UUID branchId, String status, Pageable pageable);
    Optional<CustomerRegistrationPersistenceEntity> findByCustomerIdAndBranchId(UUID customerId, UUID branchId);
}






