package com.tuxlogic.shiftiq.platform.fleet.application.outboundservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;

import java.util.Optional;
import java.util.UUID;

/**
 * Anti-Corruption Layer (ACL) Outbound Port to verify entity existence in core context.
 */
public interface ExternalCoreService {
    boolean existsBranchById(BranchId branchId);
    boolean existsCustomerById(CustomerId customerId);
    boolean existsEmployeeById(EmployeeId employeeId);
    Optional<UUID> findUserIdByEmployeeId(EmployeeId employeeId);
    Optional<UUID> findWorkshopIdForBranch(BranchId branchId);
    boolean existsActiveWorkshopSpecialty(UUID workshopId, String code);
}
