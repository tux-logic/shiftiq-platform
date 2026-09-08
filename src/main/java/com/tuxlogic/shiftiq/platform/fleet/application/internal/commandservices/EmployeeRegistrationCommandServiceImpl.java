package com.tuxlogic.shiftiq.platform.fleet.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.EmployeeRegistrationCommandFailure;
import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.EmployeeRegistrationCommandService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.EmployeeRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.CreateEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.UpdateEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.DeleteEmployeeRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.EmployeeRegistrationRepository;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.events.EmployeeRegistrationCreatedEvent;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeRegistrationCommandServiceImpl implements EmployeeRegistrationCommandService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeRegistrationCommandServiceImpl.class);

    private final EmployeeRegistrationRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public EmployeeRegistrationCommandServiceImpl(EmployeeRegistrationRepository repository,
                                                   ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Result<EmployeeRegistration, EmployeeRegistrationCommandFailure> handle(
            CreateEmployeeRegistrationCommand command) {
        try {
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

            eventPublisher.publishEvent(new EmployeeRegistrationCreatedEvent(
                    this,
                    saved.getId() != null ? saved.getId().value() : null,
                    command.employeeId(),
                    command.branchId()
            ));

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