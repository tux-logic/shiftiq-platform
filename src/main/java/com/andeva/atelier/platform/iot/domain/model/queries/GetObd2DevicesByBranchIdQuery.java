package com.andeva.atelier.platform.iot.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;

/**
 * Query representing the request to retrieve all OBD2 devices belonging to a specific branch.
 */
public record GetObd2DevicesByBranchIdQuery(
        BranchId branchId
) {
    public GetObd2DevicesByBranchIdQuery {
        if (branchId == null) {
            throw new IllegalArgumentException("branchId cannot be null");
        }
    }
}
