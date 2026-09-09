package com.tuxlogic.shiftiq.platform.operations.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.Service;
import com.tuxlogic.shiftiq.platform.operations.application.commandservices.ServiceCommandFailure;
import com.tuxlogic.shiftiq.platform.operations.application.commandservices.ServiceCommandService;
import com.tuxlogic.shiftiq.platform.operations.domain.model.commands.CreateServiceCommand;
import com.tuxlogic.shiftiq.platform.operations.domain.model.commands.DeleteServiceCommand;
import com.tuxlogic.shiftiq.platform.operations.domain.model.commands.UpdateServiceCommand;
import com.tuxlogic.shiftiq.platform.operations.domain.repositories.ServiceRepository;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.OperationsMessageKeys;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@org.springframework.stereotype.Service
public class ServiceCommandServiceImpl implements ServiceCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCommandServiceImpl.class);
    private final ServiceRepository serviceRepository;

    public ServiceCommandServiceImpl(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Override
    @Transactional
    public Result<Service, ServiceCommandFailure> handle(CreateServiceCommand command) {
        try {
            var service = new Service(
                    command.branchId(),
                    command.name(),
                    command.price()
            );

            var savedService = serviceRepository.save(service);
            return Result.success(savedService);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Failed to create service due to invalid data: {}", e.getMessage());
            return Result.failure(new ServiceCommandFailure.InvalidData(e.getMessage()));
        } catch (Exception e) {
            LOGGER.error("Failed to create service: {}", e.getMessage(), e);
            return Result.failure(new ServiceCommandFailure.InvalidData(OperationsMessageKeys.UNEXPECTED_ERROR));
        }
    }

    @Override
    @Transactional
    public Result<Service, ServiceCommandFailure> handle(UpdateServiceCommand command) {
        try {
            var result = serviceRepository.findById(command.serviceId());
            if (result.isEmpty()) {
                LOGGER.warn("Service not found for update with ID: {}", command.serviceId());
                return Result.failure(new ServiceCommandFailure.NotFound(OperationsMessageKeys.SERVICE_NOT_FOUND));
            }

            var service = result.get();
            service.update(command.name(), command.price());

            var savedService = serviceRepository.save(service);
            return Result.success(savedService);
        } catch (IllegalArgumentException e) {
            return Result.failure(new ServiceCommandFailure.InvalidData(e.getMessage()));
        } catch (Exception e) {
            LOGGER.error("Failed to update service with ID: {}", command.serviceId(), e);
            return Result.failure(new ServiceCommandFailure.InvalidData(OperationsMessageKeys.UNEXPECTED_ERROR));
        }
    }

    @Override
    @Transactional
    public Result<UUID, ServiceCommandFailure> handle(DeleteServiceCommand command) {
        try {
            var existingService = serviceRepository.findById(command.serviceId());
            if (existingService.isEmpty()) {
                LOGGER.warn("Service not found for deletion with ID: {}", command.serviceId());
                return Result.failure(new ServiceCommandFailure.NotFound(OperationsMessageKeys.SERVICE_NOT_FOUND));
            }

            serviceRepository.delete(existingService.get());
            return Result.success(command.serviceId().value());
        } catch (Exception e) {
            LOGGER.error("Failed to delete service with ID: {}", command.serviceId(), e);
            return Result.failure(new ServiceCommandFailure.InvalidData(OperationsMessageKeys.SERVICE_DELETE_FAILED));
        }
    }
}

