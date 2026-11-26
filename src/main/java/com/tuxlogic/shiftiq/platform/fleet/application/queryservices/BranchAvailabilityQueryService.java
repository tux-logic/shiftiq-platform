package com.tuxlogic.shiftiq.platform.fleet.application.queryservices;

import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.resources.BranchAvailabilityResource;

import java.time.LocalDate;
import java.util.UUID;

public interface BranchAvailabilityQueryService {
    BranchAvailabilityResource getAvailability(UUID branchId, LocalDate date);
}