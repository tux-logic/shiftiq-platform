package com.tuxlogic.shiftiq.platform.fleet.application.outboundservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;

/**
 * Anti-Corruption Layer (ACL) Outbound Port to verify entity existence in core context.
 */
public interface ExternalCoreService {
    boolean existsBranchById(BranchId branchId);
    boolean existsCustomerById(CustomerId customerId);
    boolean existsEmployeeById(EmployeeId employeeId);
}
