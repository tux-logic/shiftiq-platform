package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.core.domain.model.entities.WorkshopSpecialtyRevision;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopSpecialtyId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.WorkshopSpecialtyHistoryRepository;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.assemblers.WorkshopSpecialtyHistoryPersistenceAssembler;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories.WorkshopSpecialtyHistoryPersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class WorkshopSpecialtyHistoryRepositoryAdapter implements WorkshopSpecialtyHistoryRepository {

    private final WorkshopSpecialtyHistoryPersistenceRepository persistenceRepository;

    public WorkshopSpecialtyHistoryRepositoryAdapter(WorkshopSpecialtyHistoryPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public WorkshopSpecialtyRevision save(WorkshopSpecialtyRevision revision) {
        var entity = WorkshopSpecialtyHistoryPersistenceAssembler.toEntity(revision);
        var saved = persistenceRepository.save(entity);
        return WorkshopSpecialtyHistoryPersistenceAssembler.toDomain(saved);
    }

    @Override
    public List<WorkshopSpecialtyRevision> findByWorkshopIdAndSpecialtyId(WorkshopId workshopId, WorkshopSpecialtyId specialtyId) {
        if (workshopId == null || specialtyId == null) {
            return List.of();
        }
        return persistenceRepository.findByWorkshopIdAndSpecialtyIdOrderByRecordedAtDesc(workshopId.value(), specialtyId.value())
                .stream()
                .map(WorkshopSpecialtyHistoryPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<WorkshopSpecialtyRevision> findByWorkshopId(WorkshopId workshopId) {
        if (workshopId == null) {
            return List.of();
        }
        return persistenceRepository.findByWorkshopIdOrderByRecordedAtDesc(workshopId.value())
                .stream()
                .map(WorkshopSpecialtyHistoryPersistenceAssembler::toDomain)
                .toList();
    }
}
