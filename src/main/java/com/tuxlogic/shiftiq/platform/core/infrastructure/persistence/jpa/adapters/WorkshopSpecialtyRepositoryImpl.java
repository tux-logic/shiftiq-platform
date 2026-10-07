package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.WorkshopSpecialty;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopSpecialtyId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.WorkshopSpecialtyRepository;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.assemblers.WorkshopSpecialtyPersistenceAssembler;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.WorkshopSpecialtyPersistenceEntity;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories.WorkshopSpecialtyPersistenceRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Transactional(readOnly = true)
public class WorkshopSpecialtyRepositoryImpl implements WorkshopSpecialtyRepository {

    private final WorkshopSpecialtyPersistenceRepository repository;

    public WorkshopSpecialtyRepositoryImpl(WorkshopSpecialtyPersistenceRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public WorkshopSpecialty save(WorkshopSpecialty specialty) {
        var entity = JpaAdapterUtils.resolveEntity(
                specialty.getId() != null ? specialty.getId().value() : null,
                repository::findById,
                WorkshopSpecialtyPersistenceEntity::new
        );
        WorkshopSpecialtyPersistenceAssembler.toEntity(specialty, entity);
        return WorkshopSpecialtyPersistenceAssembler.toDomain(repository.save(entity));
    }

    @Override
    public Optional<WorkshopSpecialty> findById(WorkshopSpecialtyId id) {
        return repository.findById(id.value()).map(WorkshopSpecialtyPersistenceAssembler::toDomain);
    }

    @Override
    public List<WorkshopSpecialty> findAllByWorkshopId(WorkshopId workshopId) {
        return repository.findAllByWorkshopId(workshopId.value()).stream()
                .map(WorkshopSpecialtyPersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkshopSpecialty> findAllActiveByWorkshopId(WorkshopId workshopId) {
        return repository.findAllByWorkshopIdAndActiveTrue(workshopId.value()).stream()
                .map(WorkshopSpecialtyPersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<WorkshopSpecialty> findByWorkshopIdAndCode(WorkshopId workshopId, String code) {
        return repository.findByWorkshopIdAndCode(workshopId.value(), code.toUpperCase())
                .map(WorkshopSpecialtyPersistenceAssembler::toDomain);
    }

    @Override
    public boolean existsByWorkshopIdAndCode(WorkshopId workshopId, String code) {
        return repository.existsByWorkshopIdAndCode(workshopId.value(), code.toUpperCase());
    }

    @Override
    @Transactional
    public void delete(WorkshopSpecialty specialty) {
        if (specialty.getId() != null) {
            repository.deleteById(specialty.getId().value());
        }
    }
}
