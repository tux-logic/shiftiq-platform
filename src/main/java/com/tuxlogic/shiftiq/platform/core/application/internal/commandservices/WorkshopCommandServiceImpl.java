package com.tuxlogic.shiftiq.platform.core.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.WorkshopCommandService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Workshop;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateWorkshopCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateWorkshopCommand;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.OwnerRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.WorkshopRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WorkshopCommandServiceImpl implements WorkshopCommandService {

    private static final Logger log = LoggerFactory.getLogger(WorkshopCommandServiceImpl.class);

    private final WorkshopRepository workshopRepository;
    private final OwnerRepository ownerRepository;

    public WorkshopCommandServiceImpl(WorkshopRepository workshopRepository, OwnerRepository ownerRepository) {
        this.workshopRepository = workshopRepository;
        this.ownerRepository = ownerRepository;
    }

    @Override
    public Optional<Workshop> handle(CreateWorkshopCommand command) {
        if (!ownerRepository.existsById(command.ownerId())) {
            throw new IllegalArgumentException("core.error.owner.notFound");
        }

        var workshop = new Workshop(
                command.ownerId(),
                command.businessName(),
                command.brandName(),
                command.taxId(),
                command.mileageIntervalConfig()
        );

        var savedWorkshop = workshopRepository.save(workshop);
        log.info("Created Workshop ID '{}' for owner ID '{}'", savedWorkshop.getId().value(), command.ownerId().value());
        return Optional.of(savedWorkshop);
    }

    @Override
    public Optional<Workshop> handle(UpdateWorkshopCommand command) {
        var result = workshopRepository.findById(command.id());
        if (result.isEmpty()) throw new IllegalArgumentException("core.error.workshop.notFound");

        var workshop = result.get();
        workshop.update(
            command.businessName(),
            command.brandName(),
            command.taxId(),
            command.mileageIntervalConfig()
        );

        var savedWorkshop = workshopRepository.save(workshop);
        log.info("Updated Workshop ID '{}'", savedWorkshop.getId().value());
        return Optional.of(savedWorkshop);
    }
}
