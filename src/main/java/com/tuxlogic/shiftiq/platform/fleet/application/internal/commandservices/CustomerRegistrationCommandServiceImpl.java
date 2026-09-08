package com.tuxlogic.shiftiq.platform.fleet.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.CustomerRegistrationCommandFailure;
import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.CustomerRegistrationCommandService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.CustomerRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.CreateCustomerRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.DeleteCustomerRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.UpdateCustomerRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.CustomerRegistrationRepository;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.events.CustomerRegistrationCreatedEvent;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomerRegistrationCommandServiceImpl implements CustomerRegistrationCommandService {

    private static final Logger log = LoggerFactory.getLogger(CustomerRegistrationCommandServiceImpl.class);

    private final CustomerRegistrationRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public CustomerRegistrationCommandServiceImpl(CustomerRegistrationRepository repository,
                                                   ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Result<CustomerRegistration, CustomerRegistrationCommandFailure> handle(CreateCustomerRegistrationCommand command) {
        try {
            boolean exists = repository.existsByCustomerIdAndBranchId(command.customerId().value(), command.branchId().value());
            if (exists) {
                log.warn("Customer registration conflict: customer {} already registered in branch {}", command.customerId(), command.branchId());
                return Result.failure(CustomerRegistrationCommandFailure.REGISTRATION_ALREADY_EXISTS);
            }

            var registration = new CustomerRegistration(command.customerId().value(), command.branchId());
            var saved = repository.save(registration);

            eventPublisher.publishEvent(new CustomerRegistrationCreatedEvent(
                    this,
                    saved.getId() != null ? saved.getId().value() : null,
                    command.customerId(),
                    command.branchId()
            ));

            log.info("Customer registration created successfully with ID {}", saved.getId());
            return Result.success(saved);

        } catch (IllegalArgumentException ex) {
            log.error("Failed to create customer registration: {}", ex.getMessage());
            return Result.failure(CustomerRegistrationCommandFailure.INVALID_REGISTRATION_DATA);
        }
    }

    @Override
    @Transactional
    public Result<CustomerRegistration, CustomerRegistrationCommandFailure> handle(UpdateCustomerRegistrationCommand command) {
        try {
            var opt = repository.findById(command.registrationId());
            if (opt.isEmpty()) {
                return Result.failure(CustomerRegistrationCommandFailure.REGISTRATION_NOT_FOUND);
            }

            var registration = opt.get();

            // Only status update supported for now (deactivate)
            if (command.status().value().equals("INACTIVE")) {
                registration.deactivate();
            }

            var updated = repository.save(registration);
            return Result.success(updated);

        } catch (IllegalArgumentException ex) {
            return Result.failure(CustomerRegistrationCommandFailure.INVALID_REGISTRATION_DATA);
        }
    }

    @Override
    @Transactional
    public Result<UUID, CustomerRegistrationCommandFailure> handle(DeleteCustomerRegistrationCommand command) {
        try {
            var opt = repository.findById(command.registrationId());
            if (opt.isEmpty()) {
                return Result.failure(CustomerRegistrationCommandFailure.REGISTRATION_NOT_FOUND);
            }

            var registration = opt.get();
            registration.deactivate();
            repository.save(registration);
            return Result.success(command.registrationId());

        } catch (IllegalArgumentException ex) {
            return Result.failure(CustomerRegistrationCommandFailure.INVALID_REGISTRATION_DATA);
        }
    }
}

