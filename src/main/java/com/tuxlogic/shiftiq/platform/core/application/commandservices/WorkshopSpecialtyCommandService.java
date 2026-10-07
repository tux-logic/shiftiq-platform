package com.tuxlogic.shiftiq.platform.core.application.commandservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.WorkshopSpecialty;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateWorkshopSpecialtyCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.DeactivateWorkshopSpecialtyCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateWorkshopSpecialtyCommand;

import java.util.Optional;

public interface WorkshopSpecialtyCommandService {
    Optional<WorkshopSpecialty> handle(CreateWorkshopSpecialtyCommand command);
    Optional<WorkshopSpecialty> handle(UpdateWorkshopSpecialtyCommand command);
    boolean handle(DeactivateWorkshopSpecialtyCommand command);
}
