package com.tuxlogic.shiftiq.platform.core.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.WorkshopSpecialtyCommandService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.WorkshopSpecialty;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateWorkshopSpecialtyCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.DeactivateWorkshopSpecialtyCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateWorkshopSpecialtyCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.entities.WorkshopSpecialtyRevision;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.WorkshopRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.WorkshopSpecialtyHistoryRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.WorkshopSpecialtyRepository;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.AuthenticatedPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class WorkshopSpecialtyCommandServiceImpl implements WorkshopSpecialtyCommandService {

    private static final Logger log = LoggerFactory.getLogger(WorkshopSpecialtyCommandServiceImpl.class);

    private final WorkshopSpecialtyRepository specialtyRepository;
    private final WorkshopRepository workshopRepository;
    private final WorkshopSpecialtyHistoryRepository historyRepository;

    public WorkshopSpecialtyCommandServiceImpl(WorkshopSpecialtyRepository specialtyRepository,
                                              WorkshopRepository workshopRepository,
                                              WorkshopSpecialtyHistoryRepository historyRepository) {
        this.specialtyRepository = specialtyRepository;
        this.workshopRepository = workshopRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public Optional<WorkshopSpecialty> handle(CreateWorkshopSpecialtyCommand command) {
        if (!workshopRepository.existsById(command.workshopId())) {
            log.warn("Cannot create specialty: workshop {} not found", command.workshopId().value());
            throw new IllegalArgumentException("core.error.workshop.notFound");
        }

        if (specialtyRepository.existsByWorkshopIdAndCode(command.workshopId(), command.code())) {
            log.warn("Cannot create specialty: code {} already exists for workshop {}",
                    command.code(), command.workshopId().value());
            throw new IllegalArgumentException("core.error.specialtyCode.alreadyExists");
        }

        var specialty = new WorkshopSpecialty(
                command.workshopId(),
                command.name(),
                command.code(),
                command.description()
        );

        var saved = specialtyRepository.save(specialty);
        recordHistory(saved, "CREATED");
        log.info("Created specialty {} ({}) for workshop {}",
                saved.getName(), saved.getCode(), command.workshopId().value());
        return Optional.of(saved);
    }

    @Override
    public Optional<WorkshopSpecialty> handle(UpdateWorkshopSpecialtyCommand command) {
        var existing = specialtyRepository.findById(command.specialtyId());
        if (existing.isEmpty()) {
            log.warn("Cannot update specialty: id {} not found", command.specialtyId().value());
            return Optional.empty();
        }

        var specialty = existing.get();
        specialty.update(command.name(), command.description());
        var saved = specialtyRepository.save(specialty);
        recordHistory(saved, "UPDATED");
        log.info("Updated specialty id {}", command.specialtyId().value());
        return Optional.of(saved);
    }

    @Override
    public boolean handle(DeactivateWorkshopSpecialtyCommand command) {
        var existing = specialtyRepository.findById(command.specialtyId());
        if (existing.isEmpty()) {
            log.warn("Cannot deactivate specialty: id {} not found", command.specialtyId().value());
            return false;
        }

        var specialty = existing.get();
        specialty.deactivate();
        specialtyRepository.save(specialty);
        recordHistory(specialty, "DEACTIVATED");
        log.info("Deactivated specialty id {}", command.specialtyId().value());
        return true;
    }

    @Override
    public void seedDefaultSpecialties(WorkshopId workshopId) {
        if (workshopId == null) {
            return;
        }
        for (var defaultSpecialty : DEFAULT_SPECIALTIES) {
            if (specialtyRepository.existsByWorkshopIdAndCode(workshopId, defaultSpecialty.code())) {
                continue;
            }
            var specialty = new WorkshopSpecialty(
                    workshopId, defaultSpecialty.name(), defaultSpecialty.code(), defaultSpecialty.description());
            var saved = specialtyRepository.save(specialty);
            recordHistory(saved != null ? saved : specialty, "SEEDED");
        }
        log.info("Seeded default specialties for workshop {}", workshopId.value());
    }

    private void recordHistory(WorkshopSpecialty specialty, String changeType) {
        if (specialty == null || specialty.getId() == null) {
            return;
        }
        try {
            var revision = WorkshopSpecialtyRevision.create(
                    specialty.getId(),
                    specialty.getWorkshopId(),
                    specialty.getName(),
                    specialty.getCode(),
                    specialty.getDescription(),
                    specialty.isActive(),
                    changeType,
                    getCurrentUserId()
            );
            historyRepository.save(revision);
            log.debug("Recorded workshop specialty revision for specialty {}", specialty.getId());
        } catch (Exception ex) {
            log.error("Failed to record workshop specialty revision for specialty {}: {}",
                    specialty.getId(), ex.getMessage());
        }
    }

    private UUID getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedPrincipal principal) {
            return principal.getId();
        }
        return null;
    }

    private record DefaultSpecialty(String name, String code, String description) {}

    private static final List<DefaultSpecialty> DEFAULT_SPECIALTIES = List.of(
            new DefaultSpecialty("Mecánica General", "GENERAL_MECHANIC",
                    "Mantenimiento preventivo, correctivo y reparación de motores y sistemas mecánicos"),
            new DefaultSpecialty("Electricidad y Electrónica", "ELECTRICIAN",
                    "Diagnóstico y reparación de circuitos eléctricos, sensores y cableado automotriz"),
            new DefaultSpecialty("Planchado y Pintura", "BODYWORK_PAINT",
                    "Reparación de carrocería, desabollado, pintura al horno y acabados"),
            new DefaultSpecialty("Diagnóstico Computarizado", "DIAGNOSTIC",
                    "Escaneo OBD-II, lectura de DTCs y reprogramación de módulos electrónicos"),
            new DefaultSpecialty("Alineación y Suspensión", "TIRE_ALIGNMENT",
                    "Alineación, balanceo, frenos, amortiguadores y tren delantero"));
}
