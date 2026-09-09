package com.tuxlogic.shiftiq.platform.operations.application.commandservices;

import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.Service;
import com.tuxlogic.shiftiq.platform.operations.domain.model.commands.CreateServiceCommand;
import com.tuxlogic.shiftiq.platform.operations.domain.model.commands.DeleteServiceCommand;
import com.tuxlogic.shiftiq.platform.operations.domain.model.commands.UpdateServiceCommand;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;

import java.util.UUID;

public interface ServiceCommandService {
    Result<Service, ServiceCommandFailure> handle(CreateServiceCommand command);
    Result<Service, ServiceCommandFailure> handle(UpdateServiceCommand command);
    Result<UUID, ServiceCommandFailure> handle(DeleteServiceCommand command);
}

