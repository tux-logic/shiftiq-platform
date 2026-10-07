package com.tuxlogic.shiftiq.platform.core.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.WorkshopSpecialtyCommandService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.WorkshopSpecialty;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateWorkshopSpecialtyCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.DeactivateWorkshopSpecialtyCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateWorkshopSpecialtyCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.WorkshopRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.WorkshopSpecialtyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WorkshopSpecialtyCommandServiceImpl implements WorkshopSpecialtyCommandService {

    private static final Logger log = LoggerFactory.getLogger(WorkshopSpecialtyCommandServiceImpl.class);

    private final WorkshopSpecialtyRepository specialtyRepository;
    private final WorkshopRepository workshopRepository;

    public WorkshopSpecialtyCommandServiceImpl(WorkshopSpecialtyRepository specialtyRepository,
                                              WorkshopRepository workshopRepository) {
        this.specialtyRepository = specialtyRepository;
        this.workshopRepository = workshopRepository;
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
            specialtyRepository.save(new WorkshopSpecialty(
                    workshopId, defaultSpecialty.name(), defaultSpecialty.code(), defaultSpecialty.description()));
        }
        log.info("Seeded default specialties for workshop {}", workshopId.value());
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
