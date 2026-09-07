package com.tuxlogic.shiftiq.platform.core.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.EmployeeCommandService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Employee;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateEmployeeCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.DeleteEmployeeCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateEmployeeCommand;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmployeeCommandServiceImpl implements EmployeeCommandService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeCommandServiceImpl.class);

    private final EmployeeRepository employeeRepository;

    public EmployeeCommandServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Optional<Employee> handle(CreateEmployeeCommand command) {
        if (employeeRepository.existsByUserId(command.userId())) {
            throw new IllegalArgumentException("core.error.employee.profileAlreadyExists");
        }

        var employee = new Employee(
                command.userId(),
                command.name(),
                command.document(),
                command.phone()
        );

        var savedEmployee = employeeRepository.save(employee);
        log.info("Created Employee profile ID '{}' for user ID '{}'", savedEmployee.getId().value(), command.userId().value());
        return Optional.of(savedEmployee);
    }

    @Override
    public Optional<Employee> handle(UpdateEmployeeCommand command) {
        var employee = employeeRepository.findById(command.employeeId())
                .orElseThrow(() -> new IllegalArgumentException("core.error.employee.notFound"));
        
        employee.update(
            command.name(),
            command.document(),
            command.phone()
        );

        var savedEmployee = employeeRepository.save(employee);
        log.info("Updated Employee profile ID '{}'", savedEmployee.getId().value());
        return Optional.of(savedEmployee);
    }

    @Override
    public void handle(DeleteEmployeeCommand command) {
        var existingEmployee = employeeRepository.findById(command.employeeId())
                .orElseThrow(() -> new IllegalArgumentException("core.error.employee.notFound"));
        
        employeeRepository.delete(existingEmployee);
        log.info("Deleted Employee profile ID '{}'", command.employeeId().value());
    }
}
