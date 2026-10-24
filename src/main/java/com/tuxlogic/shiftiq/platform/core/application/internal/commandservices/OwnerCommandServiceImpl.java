package com.tuxlogic.shiftiq.platform.core.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.OwnerCommandService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Owner;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateOwnerCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.DeleteOwnerCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateOwnerCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Document;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.DocumentType;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.OwnerRepository;
import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.identity.IdentityValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class OwnerCommandServiceImpl implements OwnerCommandService {

    private static final Logger log = LoggerFactory.getLogger(OwnerCommandServiceImpl.class);

    private final OwnerRepository ownerRepository;
    private final IdentityValidationService identityValidationService;

    public OwnerCommandServiceImpl(OwnerRepository ownerRepository,
                                   IdentityValidationService identityValidationService) {
        this.ownerRepository = ownerRepository;
        this.identityValidationService = identityValidationService;
    }

    @Override
    public Optional<Owner> handle(CreateOwnerCommand command) {
        if (ownerRepository.existsByUserId(command.userId())) {
            throw new IllegalArgumentException("core.error.owner.profileAlreadyExists");
        }

        validateDocument(command.document());

        var owner = new Owner(
                command.userId(),
                command.name(),
                command.document(),
                command.phone()
        );

        var savedOwner = ownerRepository.save(owner);
        log.info("Created Owner profile ID '{}' for user ID '{}'", savedOwner.getId().value(), command.userId().value());
        return Optional.of(savedOwner);
    }

    @Override
    public Optional<Owner> handle(UpdateOwnerCommand command) {
        var owner = ownerRepository.findById(command.ownerId())
                .orElseThrow(() -> new IllegalArgumentException("core.error.owner.notFound"));

        validateDocument(command.document());

        owner.update(
            command.name(),
            command.document(),
            command.phone()
        );

        var savedOwner = ownerRepository.save(owner);
        log.info("Updated Owner profile ID '{}'", savedOwner.getId().value());
        return Optional.of(savedOwner);
    }

    private void validateDocument(Document document) {
        if (document == null) {
            return;
        }
        var docType = document.getDocumentType();
        String docNumber = document.getDocumentNumber();

        if (docType == DocumentType.DNI) {
            var personOpt = identityValidationService.validateDni(docNumber);
            if (personOpt.isEmpty()) {
                throw new IllegalArgumentException("shared.error.identity.notFound");
            }
        } else if (docType == DocumentType.CE) {
            var personOpt = identityValidationService.validateCe(docNumber);
            if (personOpt.isEmpty()) {
                throw new IllegalArgumentException("shared.error.identity.notFound");
            }
        } else if (docType == DocumentType.RUC) {
            var companyOpt = identityValidationService.validateRuc(docNumber);
            if (companyOpt.isEmpty()) {
                throw new IllegalArgumentException("shared.error.identity.notFound");
            }
            if (!companyOpt.get().active()) {
                throw new IllegalArgumentException("shared.error.identity.rucInactive");
            }
        }
    }

    @Override
    public void handle(DeleteOwnerCommand command) {
        var existingOwner = ownerRepository.findById(command.ownerId())
                .orElseThrow(() -> new IllegalArgumentException("core.error.owner.notFound"));
        
        ownerRepository.delete(existingOwner);
        log.info("Deleted Owner profile ID '{}'", command.ownerId().value());
    }
}
