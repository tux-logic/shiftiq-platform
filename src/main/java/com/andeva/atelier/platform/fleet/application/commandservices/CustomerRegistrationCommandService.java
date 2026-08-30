package com.andeva.atelier.platform.fleet.application.commandservices;

import com.andeva.atelier.platform.fleet.domain.model.aggregates.CustomerRegistration;
import com.andeva.atelier.platform.fleet.domain.model.commands.CreateCustomerRegistrationCommand;
import com.andeva.atelier.platform.fleet.domain.model.commands.DeleteCustomerRegistrationCommand;
import com.andeva.atelier.platform.fleet.domain.model.commands.UpdateCustomerRegistrationCommand;
import com.andeva.atelier.platform.shared.application.result.Result;

import java.util.UUID;

public interface CustomerRegistrationCommandService {

    Result<CustomerRegistration, CustomerRegistrationCommandFailure> handle(CreateCustomerRegistrationCommand command);

    Result<CustomerRegistration, CustomerRegistrationCommandFailure> handle(UpdateCustomerRegistrationCommand command);

    Result<UUID, CustomerRegistrationCommandFailure> handle(DeleteCustomerRegistrationCommand command);
}

