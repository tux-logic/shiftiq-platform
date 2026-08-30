package com.andeva.atelier.platform.fleet.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.fleet.infrastructure.persistence.jpa.entities.EmployeeRegistrationPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRegistrationPersistenceRepository extends JpaRepository<EmployeeRegistrationPersistenceEntity, UUID> {
    Optional<EmployeeRegistrationPersistenceEntity> findByEmployeeIdAndBranchId(UUID employeeId, UUID branchId);
    Optional<EmployeeRegistrationPersistenceEntity> findByEmployeeId(UUID employeeId);
    List<EmployeeRegistrationPersistenceEntity> findByBranchId(UUID branchId);
    List<EmployeeRegistrationPersistenceEntity> findByBranchIdAndStatus(UUID branchId, String status);
}
