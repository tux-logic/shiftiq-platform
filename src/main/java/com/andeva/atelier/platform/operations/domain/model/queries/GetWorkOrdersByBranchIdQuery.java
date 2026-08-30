package com.andeva.atelier.platform.operations.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;

/**
 * Query to retrieve all Work Orders associated with a specific Branch ID. This query encapsulates the necessary information to perform the retrieval operation, which is the Branch ID.
 * @param branchId
 * @author Joel Huamani Estefanero
 */
public record GetWorkOrdersByBranchIdQuery(BranchId branchId) {}