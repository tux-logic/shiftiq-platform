package com.tuxlogic.shiftiq.platform.core.application.commandservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Employee;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateEmployeeCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.DeleteEmployeeCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateEmployeeCommand;

import java.util.Optional;

public interface EmployeeCommandService {
    Optional<Employee> handle(CreateEmployeeCommand command);
    Optional<Employee> handle(UpdateEmployeeCommand command);
    void handle(DeleteEmployeeCommand command);
}
