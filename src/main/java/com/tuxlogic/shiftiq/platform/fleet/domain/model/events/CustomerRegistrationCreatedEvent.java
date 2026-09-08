package com.tuxlogic.shiftiq.platform.fleet.domain.model.events;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;

import java.util.UUID;

/**
 * Domain event published when a Customer is registered to a Branch in fleet context.
 */
public record CustomerRegistrationCreatedEvent(
        Object source,
        UUID registrationId,
        CustomerId customerId,
        BranchId branchId
) {}
