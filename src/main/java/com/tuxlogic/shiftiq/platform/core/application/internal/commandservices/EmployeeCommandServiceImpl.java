package com.tuxlogic.shiftiq.platform.core.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.EmployeeCommandService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Employee;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateEmployeeCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.DeleteEmployeeCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateEmployeeCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Document;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.DocumentType;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.EmployeeRepository;
import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.identity.IdentityValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class EmployeeCommandServiceImpl implements EmployeeCommandService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeCommandServiceImpl.class);

    private final EmployeeRepository employeeRepository;
    private final IdentityValidationService identityValidationService;

    public EmployeeCommandServiceImpl(EmployeeRepository employeeRepository,
                                      IdentityValidationService identityValidationService) {
        this.employeeRepository = employeeRepository;
        this.identityValidationService = identityValidationService;
    }

    @Override
    public Optional<Employee> handle(CreateEmployeeCommand command) {
        if (employeeRepository.existsByUserId(command.userId())) {
            throw new IllegalArgumentException("core.error.employee.profileAlreadyExists");
        }

        validateDocument(command.document());

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

        validateDocument(command.document());

        employee.update(
            command.name(),
            command.document(),
            command.phone()
        );

        var savedEmployee = employeeRepository.save(employee);
        log.info("Updated Employee profile ID '{}'", savedEmployee.getId().value());
        return Optional.of(savedEmployee);
    }

    private void validateDocument(Document document) {
        if (document == null) {
            return;
        }
        var docType = document.getDocumentType();
        String docNumber = document.getDocumentNumber();

        if (docType == DocumentType.DNI) {
            var personOpt = identityValidationService.validateDni(docNumber);
            if (personOpt.isEmpty()) {
                throw new IllegalArgumentException("shared.error.identity.notFound");
            }
        } else if (docType == DocumentType.CE) {
            var personOpt = identityValidationService.validateCe(docNumber);
            if (personOpt.isEmpty()) {
                throw new IllegalArgumentException("shared.error.identity.notFound");
            }
        }
    }

    @Override
    public void handle(DeleteEmployeeCommand command) {
        var existingEmployee = employeeRepository.findById(command.employeeId())
                .orElseThrow(() -> new IllegalArgumentException("core.error.employee.notFound"));
        
        employeeRepository.delete(existingEmployee);
        log.info("Deleted Employee profile ID '{}'", command.employeeId().value());
    }
}
