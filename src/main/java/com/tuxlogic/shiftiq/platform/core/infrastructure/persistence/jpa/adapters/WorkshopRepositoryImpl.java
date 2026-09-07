package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Workshop;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.OwnerId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.WorkshopRepository;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.assemblers.WorkshopPersistenceAssembler;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.WorkshopPersistenceEntity;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories.WorkshopPersistenceRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class WorkshopRepositoryImpl implements WorkshopRepository {

    private final WorkshopPersistenceRepository workshopPersistenceRepository;

    public WorkshopRepositoryImpl(WorkshopPersistenceRepository workshopPersistenceRepository) {
        this.workshopPersistenceRepository = workshopPersistenceRepository;
    }

    @Override
    @Transactional
    public Workshop save(Workshop workshop) {
        var entity = JpaAdapterUtils.resolveEntity(
                workshop.getId() != null ? workshop.getId().value() : null,
                workshopPersistenceRepository::findById,
                WorkshopPersistenceEntity::new
        );
        WorkshopPersistenceAssembler.toEntity(workshop, entity);
        return WorkshopPersistenceAssembler.toDomain(workshopPersistenceRepository.save(entity));
    }

    @Override
    public Optional<Workshop> findById(WorkshopId id) {
        return workshopPersistenceRepository.findById(id.value()).map(WorkshopPersistenceAssembler::toDomain);
    }

    @Override
    public List<Workshop> findAllByOwnerId(OwnerId ownerId) {
        return workshopPersistenceRepository.findAllByOwnerId(ownerId.value()).stream()
                .map(WorkshopPersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(WorkshopId id) {
        return workshopPersistenceRepository.existsById(id.value());
    }
}
