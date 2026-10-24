package com.tuxlogic.shiftiq.platform.core.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.WorkshopCommandService;
import com.tuxlogic.shiftiq.platform.core.application.commandservices.WorkshopSpecialtyCommandService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Workshop;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateWorkshopCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateWorkshopCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.TaxId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.OwnerRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.WorkshopRepository;
import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.identity.IdentityValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class WorkshopCommandServiceImpl implements WorkshopCommandService {

    private static final Logger log = LoggerFactory.getLogger(WorkshopCommandServiceImpl.class);

    private final WorkshopRepository workshopRepository;
    private final OwnerRepository ownerRepository;
    private final WorkshopSpecialtyCommandService workshopSpecialtyCommandService;
    private final IdentityValidationService identityValidationService;

    public WorkshopCommandServiceImpl(WorkshopRepository workshopRepository, OwnerRepository ownerRepository,
                                      WorkshopSpecialtyCommandService workshopSpecialtyCommandService,
                                      IdentityValidationService identityValidationService) {
        this.workshopRepository = workshopRepository;
        this.ownerRepository = ownerRepository;
        this.workshopSpecialtyCommandService = workshopSpecialtyCommandService;
        this.identityValidationService = identityValidationService;
    }

    @Override
    public Optional<Workshop> handle(CreateWorkshopCommand command) {
        if (!ownerRepository.existsById(command.ownerId())) {
            throw new IllegalArgumentException("core.error.owner.notFound");
        }

        validateTaxId(command.taxId());

        var workshop = new Workshop(
                command.ownerId(),
                command.businessName(),
                command.brandName(),
                command.taxId(),
                command.mileageIntervalConfig()
        );

        var savedWorkshop = workshopRepository.save(workshop);
        workshopSpecialtyCommandService.seedDefaultSpecialties(savedWorkshop.getId());
        log.info("Created Workshop ID '{}' for owner ID '{}'", savedWorkshop.getId().value(), command.ownerId().value());
        return Optional.of(savedWorkshop);
    }

    @Override
    public Optional<Workshop> handle(UpdateWorkshopCommand command) {
        var workshop = workshopRepository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("core.error.workshop.notFound"));

        validateTaxId(command.taxId());

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

    private void validateTaxId(TaxId taxId) {
        if (taxId == null || taxId.value() == null) {
            return;
        }
        var companyOpt = identityValidationService.validateRuc(taxId.value());
        if (companyOpt.isEmpty()) {
            throw new IllegalArgumentException("shared.error.identity.notFound");
        }
        var company = companyOpt.get();
        if (!company.active()) {
            throw new IllegalArgumentException("shared.error.identity.rucInactive");
        }
        if (!company.habido()) {
            throw new IllegalArgumentException("shared.error.identity.rucNotHabido");
        }
    }
}
