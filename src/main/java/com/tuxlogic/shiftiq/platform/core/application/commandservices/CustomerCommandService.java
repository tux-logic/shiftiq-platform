package com.tuxlogic.shiftiq.platform.core.application.commandservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Customer;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateCustomerCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.DeleteCustomerCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateCustomerCommand;

import java.util.Optional;

public interface CustomerCommandService {
    Optional<Customer> handle(CreateCustomerCommand command);
    Optional<Customer> handle(UpdateCustomerCommand command);
    void handle(DeleteCustomerCommand command);
}
