package com.tuxlogic.shiftiq.platform.shared.domain.model.events;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import java.util.UUID;

/**
 * Integration event published when an employee registration request is approved.
 *
 * <p>It lives in the shared kernel because it crosses bounded contexts: the fleet
 * context publishes it when a registration moves to {@code ACTIVE} and the IAM
 * context consumes it to grant the new employee access to the branch. Identifiers
 * are plain {@link UUID}s so the shared kernel does not depend on any context's
 * value objects.</p>
 *
 * @param source         the aggregate that registered the event
 * @param registrationId the identifier of the approved employee registration
 * @param employeeId     the identifier of the employee joining the branch
 * @param branchId       the branch the employee is joining
 */
public record EmployeeRegistrationApprovedEvent(
        Object source,
        UUID registrationId,
        UUID employeeId,
        BranchId branchId
) {}
