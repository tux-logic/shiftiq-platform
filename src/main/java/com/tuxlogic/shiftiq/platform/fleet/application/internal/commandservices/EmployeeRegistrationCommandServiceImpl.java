package com.tuxlogic.shiftiq.platform.fleet.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.EmployeeRegistrationCommandFailure;
import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.EmployeeRegistrationCommandService;
import com.tuxlogic.shiftiq.platform.fleet.application.outboundservices.ExternalCoreService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.EmployeeRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.CreateEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.UpdateEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.DeleteEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.EmployeeRegistrationRepository;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class EmployeeRegistrationCommandServiceImpl implements EmployeeRegistrationCommandService {

    private final EmployeeRegistrationRepository repository;
    private final ExternalCoreService externalCoreService;

    public EmployeeRegistrationCommandServiceImpl(EmployeeRegistrationRepository repository, ExternalCoreService externalCoreService) {
        this.repository = repository;
        this.externalCoreService = externalCoreService;
    }

    @Override
    @Transactional
    public Result<EmployeeRegistration, EmployeeRegistrationCommandFailure> handle(
            CreateEmployeeRegistrationCommand command) {
        try {
            if (!externalCoreService.existsBranchById(command.branchId())) {
                log.warn("Employee registration failed: Branch {} does not exist", command.branchId());
                return Result.failure(EmployeeRegistrationCommandFailure.INVALID_REGISTRATION_DATA);
            }
            if (!externalCoreService.existsEmployeeById(command.employeeId())) {
                log.warn("Employee registration failed: Employee {} does not exist", command.employeeId());
                return Result.failure(EmployeeRegistrationCommandFailure.INVALID_REGISTRATION_DATA);
            }

            if (repository.existsByEmployeeIdAndBranchId(command.employeeId().value(), command.branchId().value())) {
                log.warn("Employee registration conflict: employee {} already registered in branch {}", command.employeeId(), command.branchId());
                return Result.failure(EmployeeRegistrationCommandFailure.REGISTRATION_ALREADY_EXISTS);
            }

            var registration = new EmployeeRegistration(
                    command.employeeId().value(),
                    command.branchId(),
                    command.speciality(),
                    command.specialityName(),
                    command.salary());
            var saved = repository.save(registration);

            log.info("Employee registration created successfully with ID {}", saved.getId());
            return Result.success(saved);
        } catch (IllegalArgumentException ex) {
            log.error("Failed to create employee registration: {}", ex.getMessage());
            return Result.failure(EmployeeRegistrationCommandFailure.INVALID_REGISTRATION_DATA);
        }
    }

    @Override
    @Transactional
    public Result<EmployeeRegistration, EmployeeRegistrationCommandFailure> handle(UpdateEmployeeRegistrationCommand command) {
        var registrationOptional = repository.findById(command.id());
        if (registrationOptional.isEmpty()) {
            return Result.failure(EmployeeRegistrationCommandFailure.REGISTRATION_NOT_FOUND);
        }

        var registration = registrationOptional.get();
        registration.update(command.speciality(), command.specialityName(), command.salary());
        
        var savedRegistration = repository.save(registration);
        return Result.success(savedRegistration);
    }

    @Override
    @Transactional
    public Result<EmployeeRegistration, EmployeeRegistrationCommandFailure> handle(DeleteEmployeeRegistrationCommand command) {
        var registrationOptional = repository.findById(command.id());
        if (registrationOptional.isEmpty()) {
            return Result.failure(EmployeeRegistrationCommandFailure.REGISTRATION_NOT_FOUND);
        }

        var registration = registrationOptional.get();
        registration.deactivate();
        
        var savedRegistration = repository.save(registration);
        return Result.success(savedRegistration);
    }
}