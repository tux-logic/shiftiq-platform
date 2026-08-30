package com.andeva.atelier.platform.core.application.commandservices;

import com.andeva.atelier.platform.core.domain.model.aggregates.Workshop;
import com.andeva.atelier.platform.core.domain.model.commands.CreateWorkshopCommand;
import com.andeva.atelier.platform.core.domain.model.commands.UpdateWorkshopCommand;

import java.util.Optional;

public interface WorkshopCommandService {
    Optional<Workshop> handle(CreateWorkshopCommand command);
    Optional<Workshop> handle(UpdateWorkshopCommand command);
}
