package com.tuxlogic.shiftiq.platform.fleet.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.CustomerRegistrationCommandFailure;
import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.CustomerRegistrationCommandService;
import com.tuxlogic.shiftiq.platform.fleet.application.outboundservices.ExternalCoreService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.CustomerRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.CreateCustomerRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.DeleteCustomerRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.commands.UpdateCustomerRegistrationCommand;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.CustomerRegistrationRepository;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class CustomerRegistrationCommandServiceImpl implements CustomerRegistrationCommandService {

    private final CustomerRegistrationRepository repository;
    private final ExternalCoreService externalCoreService;

    public CustomerRegistrationCommandServiceImpl(CustomerRegistrationRepository repository, ExternalCoreService externalCoreService) {
        this.repository = repository;
        this.externalCoreService = externalCoreService;
    }

    @Override
    @Transactional
    public Result<CustomerRegistration, CustomerRegistrationCommandFailure> handle(CreateCustomerRegistrationCommand command) {
        try {
            if (!externalCoreService.existsBranchById(command.branchId())) {
                log.warn("Customer registration failed: Branch {} does not exist", command.branchId());
                return Result.failure(CustomerRegistrationCommandFailure.INVALID_REGISTRATION_DATA);
            }
            if (!externalCoreService.existsCustomerById(command.customerId())) {
                log.warn("Customer registration failed: Customer {} does not exist", command.customerId());
                return Result.failure(CustomerRegistrationCommandFailure.INVALID_REGISTRATION_DATA);
            }

            boolean exists = repository.existsByCustomerIdAndBranchId(command.customerId().value(), command.branchId().value());
            if (exists) {
                log.warn("Customer registration conflict: customer {} already registered in branch {}", command.customerId(), command.branchId());
                return Result.failure(CustomerRegistrationCommandFailure.REGISTRATION_ALREADY_EXISTS);
            }

            var registration = new CustomerRegistration(command.customerId().value(), command.branchId());
            var saved = repository.save(registration);

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


