package com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.resources;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record BranchAvailabilityResource(
        UUID branchId,
        LocalDate date,
        int activeBaysCount,
        int freeBaysNow,
        List<AvailabilitySlotResource> slots
) {
    public record AvailabilitySlotResource(
            LocalTime start,
            LocalTime end,
            List<UUID> availableBayIds
    ) {
    }
}