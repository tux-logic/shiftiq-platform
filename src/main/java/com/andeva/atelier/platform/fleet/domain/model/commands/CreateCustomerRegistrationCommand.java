package com.andeva.atelier.platform.fleet.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;

public record CreateCustomerRegistrationCommand(
        CustomerId customerId,
        BranchId branchId
) {
    public CreateCustomerRegistrationCommand {
        if (customerId == null) throw new IllegalArgumentException("Customer ID is required");
        if (branchId == null) throw new IllegalArgumentException("Branch ID is required");
    }
}

