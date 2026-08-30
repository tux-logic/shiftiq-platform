package com.andeva.atelier.platform.fleet.application.commandservices;

import com.andeva.atelier.platform.fleet.domain.model.aggregates.EmployeeRegistration;
import com.andeva.atelier.platform.fleet.domain.model.commands.CreateEmployeeRegistrationCommand;
import com.andeva.atelier.platform.fleet.domain.model.commands.UpdateEmployeeRegistrationCommand;
import com.andeva.atelier.platform.fleet.domain.model.commands.DeleteEmployeeRegistrationCommand;
import com.andeva.atelier.platform.shared.application.result.Result;

public interface EmployeeRegistrationCommandService {
    Result<EmployeeRegistration, EmployeeRegistrationCommandFailure> handle(CreateEmployeeRegistrationCommand command);
    Result<EmployeeRegistration, EmployeeRegistrationCommandFailure> handle(UpdateEmployeeRegistrationCommand command);
    Result<EmployeeRegistration, EmployeeRegistrationCommandFailure> handle(DeleteEmployeeRegistrationCommand command);
}
