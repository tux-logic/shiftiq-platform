package com.andeva.atelier.platform.core.application.commandservices;

import com.andeva.atelier.platform.core.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.core.domain.model.commands.CreateCustomerCommand;
import com.andeva.atelier.platform.core.domain.model.commands.DeleteCustomerCommand;
import com.andeva.atelier.platform.core.domain.model.commands.UpdateCustomerCommand;

import java.util.Optional;

public interface CustomerCommandService {
    Optional<Customer> handle(CreateCustomerCommand command);
    Optional<Customer> handle(UpdateCustomerCommand command);
    void handle(DeleteCustomerCommand command);
}
