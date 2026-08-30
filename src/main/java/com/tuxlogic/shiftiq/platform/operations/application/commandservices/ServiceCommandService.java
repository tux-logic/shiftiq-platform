package com.tuxlogic.shiftiq.platform.operations.application.commandservices;

import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.Service;
import com.tuxlogic.shiftiq.platform.operations.domain.model.commands.CreateServiceCommand;
import com.tuxlogic.shiftiq.platform.operations.domain.model.commands.DeleteServiceCommand;
import com.tuxlogic.shiftiq.platform.operations.domain.model.commands.UpdateServiceCommand;

import java.util.Optional;

public interface ServiceCommandService {
    Optional<Service> handle(CreateServiceCommand command);
    Optional<Service> handle(UpdateServiceCommand command);
    void handle(DeleteServiceCommand command);
}
