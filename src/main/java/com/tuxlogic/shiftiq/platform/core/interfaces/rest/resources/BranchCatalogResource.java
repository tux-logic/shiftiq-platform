package com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources;

import java.util.UUID;

/**
 * Public-facing branch entry for B2C workshop discovery and booking.
 */
public record BranchCatalogResource(
        UUID branchId,
        UUID workshopId,
        String workshopBrandName,
        String workshopBusinessName,
        String branchCode,
        String branchName,
        String address,
        String phone
) {
}
