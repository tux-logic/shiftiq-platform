package com.tuxlogic.shiftiq.platform.core.domain.model.commands;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;

public record DeleteCustomerCommand(CustomerId customerId) {
}
