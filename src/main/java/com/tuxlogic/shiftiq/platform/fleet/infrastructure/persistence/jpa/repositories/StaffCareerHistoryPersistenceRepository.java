package com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.entities.StaffCareerHistoryPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StaffCareerHistoryPersistenceRepository extends JpaRepository<StaffCareerHistoryPersistenceEntity, UUID> {
    List<StaffCareerHistoryPersistenceEntity> findByBranchIdAndEmployeeIdOrderByRecordedAtDesc(UUID branchId, UUID employeeId);
    List<StaffCareerHistoryPersistenceEntity> findByRegistrationIdOrderByRecordedAtDesc(UUID registrationId);
}
