package com.tuxlogic.shiftiq.platform.fleet.domain.repositories;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.entities.StaffCareerSnapshot;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import java.util.List;
import java.util.UUID;

public interface StaffCareerHistoryRepository {
    StaffCareerSnapshot save(StaffCareerSnapshot snapshot);
    List<StaffCareerSnapshot> findByBranchIdAndEmployeeId(BranchId branchId, UUID employeeId);
    List<StaffCareerSnapshot> findByRegistrationId(UUID registrationId);
}
