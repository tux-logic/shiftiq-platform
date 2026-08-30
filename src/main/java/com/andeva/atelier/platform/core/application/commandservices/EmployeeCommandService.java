package com.andeva.atelier.platform.core.application.commandservices;

import com.andeva.atelier.platform.core.domain.model.aggregates.Employee;
import com.andeva.atelier.platform.core.domain.model.commands.CreateEmployeeCommand;
import com.andeva.atelier.platform.core.domain.model.commands.DeleteEmployeeCommand;
import com.andeva.atelier.platform.core.domain.model.commands.UpdateEmployeeCommand;

import java.util.Optional;

public interface EmployeeCommandService {
    Optional<Employee> handle(CreateEmployeeCommand command);
    Optional<Employee> handle(UpdateEmployeeCommand command);
    void handle(DeleteEmployeeCommand command);
}
