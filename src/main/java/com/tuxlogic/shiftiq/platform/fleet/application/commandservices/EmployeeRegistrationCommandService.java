package com.tuxlogic.shiftiq.platform.fleet.application.commandservices;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.EmployeeRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.CreateEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.UpdateEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.DeleteEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.RequestEmployeeJoinCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.ApproveEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.RejectEmployeeRegistrationCommand;

public interface EmployeeRegistrationCommandService {
    Result<EmployeeRegistration, EmployeeRegistrationCommandFailure> handle(CreateEmployeeRegistrationCommand command);
    Result<EmployeeRegistration, EmployeeRegistrationCommandFailure> handle(UpdateEmployeeRegistrationCommand command);
    Result<EmployeeRegistration, EmployeeRegistrationCommandFailure> handle(DeleteEmployeeRegistrationCommand command);
    Result<EmployeeRegistration, EmployeeRegistrationCommandFailure> handle(RequestEmployeeJoinCommand command);
    Result<EmployeeRegistration, EmployeeRegistrationCommandFailure> handle(ApproveEmployeeRegistrationCommand command);
    Result<EmployeeRegistration, EmployeeRegistrationCommandFailure> handle(RejectEmployeeRegistrationCommand command);
}
