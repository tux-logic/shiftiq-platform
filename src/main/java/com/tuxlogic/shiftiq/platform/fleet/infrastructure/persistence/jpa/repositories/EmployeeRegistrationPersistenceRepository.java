package com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.entities.EmployeeRegistrationPersistenceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRegistrationPersistenceRepository extends JpaRepository<EmployeeRegistrationPersistenceEntity, UUID> {
    Optional<EmployeeRegistrationPersistenceEntity> findByEmployeeIdAndBranchId(UUID employeeId, UUID branchId);
    Optional<EmployeeRegistrationPersistenceEntity> findByEmployeeId(UUID employeeId);
    Page<EmployeeRegistrationPersistenceEntity> findByBranchId(UUID branchId, Pageable pageable);
    Page<EmployeeRegistrationPersistenceEntity> findByBranchIdAndStatus(UUID branchId, String status, Pageable pageable);
}
