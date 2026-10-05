package com.tuxlogic.shiftiq.platform.fleet.domain.model.events;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import java.util.UUID;

/**
 * Domain event published when an Employee Registration request is approved.
 */
public record EmployeeRegistrationApprovedEvent(
        Object source,
        UUID registrationId,
        EmployeeId employeeId,
        BranchId branchId
) {}
