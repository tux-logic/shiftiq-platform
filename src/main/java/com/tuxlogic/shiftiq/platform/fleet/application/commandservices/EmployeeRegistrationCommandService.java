package com.tuxlogic.shiftiq.platform.fleet.application.commandservices;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.EmployeeRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.CreateEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.UpdateEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.DeleteEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;

import java.util.UUID;

public interface EmployeeRegistrationCommandService {
    Result<EmployeeRegistration, EmployeeRegistrationCommandFailure> handle(CreateEmployeeRegistrationCommand command);
    Result<EmployeeRegistration, EmployeeRegistrationCommandFailure> handle(UpdateEmployeeRegistrationCommand command);
    Result<UUID, EmployeeRegistrationCommandFailure> handle(DeleteEmployeeRegistrationCommand command);
}
