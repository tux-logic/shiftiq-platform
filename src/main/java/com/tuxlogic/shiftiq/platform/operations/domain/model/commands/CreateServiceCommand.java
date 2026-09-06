package com.tuxlogic.shiftiq.platform.operations.domain.model.commands;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;

/**
 * Command to create a new service in a branch.
 * @param branchId the ID of the branch where the service will be created
 * @param name the name of the service
 * @param price the price of the service
 * @author Joel Huamani Estefanero
 */
public record CreateServiceCommand(BranchId branchId, String name, Money price) {
    public CreateServiceCommand {
        if (branchId == null) throw new IllegalArgumentException("operations.error.command.branchId.required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("core.error.name.required");
        if (price == null) throw new IllegalArgumentException("operations.error.command.price.required");
    }
}
