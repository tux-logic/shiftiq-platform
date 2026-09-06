package com.tuxlogic.shiftiq.platform.operations.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.Service;
import com.tuxlogic.shiftiq.platform.operations.application.commandservices.ServiceCommandService;
import com.tuxlogic.shiftiq.platform.operations.domain.model.commands.CreateServiceCommand;
import com.tuxlogic.shiftiq.platform.operations.domain.model.commands.DeleteServiceCommand;
import com.tuxlogic.shiftiq.platform.operations.domain.model.commands.UpdateServiceCommand;
import com.tuxlogic.shiftiq.platform.operations.domain.repositories.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@org.springframework.stereotype.Service
public class ServiceCommandServiceImpl implements ServiceCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCommandServiceImpl.class);
    private final ServiceRepository serviceRepository;

    public ServiceCommandServiceImpl(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Override
    @Transactional
    public Optional<Service> handle(CreateServiceCommand command) {
        try {
            var service = new Service(
                    command.branchId(),
                    command.name(),
                    command.price()
            );

            var savedService = serviceRepository.save(service);
            return Optional.of(savedService);
        } catch (Exception e) {
            LOGGER.error("Failed to create service: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Optional<Service> handle(UpdateServiceCommand command) {
        try {
            var result = serviceRepository.findById(command.serviceId());
            if (result.isEmpty()) {
                LOGGER.warn("Service not found for update with ID: {}", command.serviceId());
                return Optional.empty();
            }

            var service = result.get();
            service.update(command.name(), command.price());

            var savedService = serviceRepository.save(service);
            return Optional.of(savedService);
        } catch (Exception e) {
            LOGGER.error("Failed to update service with ID: {}", command.serviceId(), e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public void handle(DeleteServiceCommand command) {
        try {
            var existingService = serviceRepository.findById(command.serviceId());
            if (existingService.isEmpty()) {
                LOGGER.warn("Service not found for deletion with ID: {}", command.serviceId());
                return;
            }

            serviceRepository.delete(existingService.get());
        } catch (Exception e) {
            LOGGER.error("Failed to delete service with ID: {}", command.serviceId(), e);
            throw new IllegalStateException("operations.error.service.deleteFailed", e);
        }
    }
}
