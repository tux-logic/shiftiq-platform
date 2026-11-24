package com.tuxlogic.shiftiq.platform.operations.domain.model.queries;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;

/**
 * Query to retrieve all work orders associated with a specific customer.
 */
public record GetWorkOrdersByCustomerIdQuery(CustomerId customerId) {
}
