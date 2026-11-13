package com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.iam.domain.model.aggregates.WorkshopRole;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.WorkshopRoleRepository;
import com.tuxlogic.shiftiq.platform.iam.infrastructure.persistence.jpa.assemblers.WorkshopRolePersistenceAssembler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class WorkshopRoleRepositoryImpl implements WorkshopRoleRepository {

    private final WorkshopRolePersistenceRepository jpaRepository;

    public WorkshopRoleRepositoryImpl(WorkshopRolePersistenceRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public WorkshopRole save(WorkshopRole role) {
        var saved = jpaRepository.save(WorkshopRolePersistenceAssembler.toEntity(role));
        return WorkshopRolePersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<WorkshopRole> findById(UUID id) {
        return jpaRepository.findById(id).map(WorkshopRolePersistenceAssembler::toDomain);
    }

    @Override
    public List<WorkshopRole> findAvailableForWorkshop(UUID workshopId) {
        return jpaRepository.findAvailableForWorkshop(workshopId).stream()
                .map(WorkshopRolePersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public Optional<WorkshopRole> findByWorkshopIdAndNameIgnoreCase(UUID workshopId, String name) {
        return jpaRepository.findByWorkshopIdAndNameIgnoreCase(workshopId, name)
                .map(WorkshopRolePersistenceAssembler::toDomain);
    }
}
