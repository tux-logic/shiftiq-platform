package com.tuxlogic.shiftiq.platform.fleet.domain.model.commands;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;

public record CreateCustomerRegistrationCommand(
        CustomerId customerId,
        BranchId branchId
) {
    public CreateCustomerRegistrationCommand {
        if (customerId == null) throw new IllegalArgumentException("Customer ID is required");
        if (branchId == null) throw new IllegalArgumentException("Branch ID is required");
    }
}

