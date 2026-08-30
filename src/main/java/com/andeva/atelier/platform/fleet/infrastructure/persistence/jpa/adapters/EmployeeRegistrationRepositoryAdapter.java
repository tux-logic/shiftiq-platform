package com.andeva.atelier.platform.fleet.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.fleet.infrastructure.persistence.jpa.entities.EmployeeRegistrationPersistenceEntity;
import com.andeva.atelier.platform.fleet.infrastructure.persistence.jpa.repositories.EmployeeRegistrationPersistenceRepository;
import com.andeva.atelier.platform.fleet.domain.model.aggregates.EmployeeRegistration;
import com.andeva.atelier.platform.fleet.domain.repositories.EmployeeRegistrationRepository;
import com.andeva.atelier.platform.fleet.infrastructure.persistence.jpa.assemblers.EmployeeRegistrationPersistenceAssembler;
import org.springframework.stereotype.Repository;

import com.andeva.atelier.platform.core.domain.model.valueobjects.EmployeeId;
import com.andeva.atelier.platform.fleet.domain.model.valueobjects.EmployeeRegistrationStatus;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public class EmployeeRegistrationRepositoryAdapter implements EmployeeRegistrationRepository {

    private final EmployeeRegistrationPersistenceRepository persistenceRepository;

    public EmployeeRegistrationRepositoryAdapter(EmployeeRegistrationPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public EmployeeRegistration save(EmployeeRegistration registration) {
        EmployeeRegistrationPersistenceEntity entity;
        if (registration.getId() != null) {
            entity = persistenceRepository.findById(registration.getId().value())
                    .orElseGet(EmployeeRegistrationPersistenceEntity::new);
        } else {
            entity = new EmployeeRegistrationPersistenceEntity();
        }
        EmployeeRegistrationPersistenceAssembler.toEntity(registration, entity);
        return EmployeeRegistrationPersistenceAssembler.toDomain(persistenceRepository.save(entity));
    }

    @Override
    public Optional<EmployeeRegistration> findById(EmployeeId id) {
        return persistenceRepository.findById(id.value())
                .map(EmployeeRegistrationPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<EmployeeRegistration> findByEmployeeId(UUID employeeId) {
        return persistenceRepository.findByEmployeeId(employeeId)
                .map(EmployeeRegistrationPersistenceAssembler::toDomain);
    }

    @Override
    public List<EmployeeRegistration> findByBranchId(BranchId branchId) {
        return persistenceRepository.findByBranchId(branchId.value())
                .stream()
                .map(EmployeeRegistrationPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<EmployeeRegistration> findByBranchIdAndStatus(BranchId branchId, EmployeeRegistrationStatus status) {
        return persistenceRepository.findByBranchIdAndStatus(branchId.value(), status.value())
                .stream()
                .map(EmployeeRegistrationPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public boolean existsByEmployeeIdAndBranchId(UUID employeeId, UUID branchId) {
        return persistenceRepository.findByEmployeeIdAndBranchId(employeeId, branchId).isPresent();
    }
}