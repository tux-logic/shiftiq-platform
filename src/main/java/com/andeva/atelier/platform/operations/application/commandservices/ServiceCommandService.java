package com.andeva.atelier.platform.operations.application.commandservices;

import com.andeva.atelier.platform.operations.domain.model.aggregates.Service;
import com.andeva.atelier.platform.operations.domain.model.commands.CreateServiceCommand;
import com.andeva.atelier.platform.operations.domain.model.commands.DeleteServiceCommand;
import com.andeva.atelier.platform.operations.domain.model.commands.UpdateServiceCommand;

import java.util.Optional;

public interface ServiceCommandService {
    Optional<Service> handle(CreateServiceCommand command);
    Optional<Service> handle(UpdateServiceCommand command);
    void handle(DeleteServiceCommand command);
}
