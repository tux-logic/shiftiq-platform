package com.andeva.atelier.platform.fleet.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.fleet.infrastructure.persistence.jpa.entities.CustomerRegistrationPersistenceEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRegistrationPersistenceRepository extends JpaRepository<CustomerRegistrationPersistenceEntity, UUID> {
    List<CustomerRegistrationPersistenceEntity> findByCustomerId(UUID customerId);
    List<CustomerRegistrationPersistenceEntity> findByBranchIdAndStatus(UUID branchId, String status);
    Optional<CustomerRegistrationPersistenceEntity> findByCustomerIdAndBranchId(UUID customerId, UUID branchId);
}

