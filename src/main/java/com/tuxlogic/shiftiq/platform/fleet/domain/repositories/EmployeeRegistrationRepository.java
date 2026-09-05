package com.tuxlogic.shiftiq.platform.fleet.domain.repositories;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.EmployeeRegistrationId;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.EmployeeRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.EmployeeRegistrationStatus;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRegistrationRepository {
    EmployeeRegistration save(EmployeeRegistration registration);
    Optional<EmployeeRegistration> findById(EmployeeRegistrationId id);
    Optional<EmployeeRegistration> findById(UUID id);
    Optional<EmployeeRegistration> findByEmployeeId(UUID employeeId);
    Page<EmployeeRegistration> findByBranchId(BranchId branchId, Pageable pageable);
    Page<EmployeeRegistration> findByBranchIdAndStatus(BranchId branchId, EmployeeRegistrationStatus status, Pageable pageable);
    boolean existsByEmployeeIdAndBranchId(UUID employeeId, UUID branchId);
}
