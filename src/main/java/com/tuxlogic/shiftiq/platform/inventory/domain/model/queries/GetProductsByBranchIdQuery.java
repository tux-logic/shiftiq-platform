package com.tuxlogic.shiftiq.platform.inventory.domain.model.queries;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

/**
 * Query representing the intention to retrieve products belonging to a specific branch,
 * optionally filtered by name, category or low-stock alert status.
 */
public record GetProductsByBranchIdQuery(
        BranchId branchId,
        String name,
        String category,
        Boolean lowStockOnly
) {
    public GetProductsByBranchIdQuery(BranchId branchId) {
        this(branchId, null, null, null);
    }
}
