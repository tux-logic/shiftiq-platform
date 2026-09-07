package com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record BranchSubscriptionResource(
        UUID id,
        UUID branchId,
        UUID planId,
        String billingCycle,
        String status,
        Instant startDate,
        Instant endDate
) {
}
