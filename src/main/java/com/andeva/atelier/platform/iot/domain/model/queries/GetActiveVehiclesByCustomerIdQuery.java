package com.andeva.atelier.platform.iot.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;

/**
 * Query representing the request to retrieve all active vehicles for a specific customer.
 */
public record GetActiveVehiclesByCustomerIdQuery(
        CustomerId customerId
) {
    public GetActiveVehiclesByCustomerIdQuery {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId cannot be null");
        }
    }
}
