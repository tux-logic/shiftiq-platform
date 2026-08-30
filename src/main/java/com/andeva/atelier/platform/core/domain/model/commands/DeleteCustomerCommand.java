package com.andeva.atelier.platform.core.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;

public record DeleteCustomerCommand(CustomerId customerId) {
}
