package com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.entities.EmployeeRegistrationPersistenceEntity;
import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.repositories.EmployeeRegistrationPersistenceRepository;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.EmployeeRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.EmployeeRegistrationRepository;
import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.assemblers.EmployeeRegistrationPersistenceAssembler;
import org.springframework.stereotype.Repository;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.EmployeeRegistrationId;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.EmployeeRegistrationStatus;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
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
    public Optional<EmployeeRegistration> findById(EmployeeRegistrationId id) {
        return persistenceRepository.findById(id.value())
                .map(EmployeeRegistrationPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<EmployeeRegistration> findByEmployeeId(UUID employeeId) {
        return persistenceRepository.findByEmployeeId(employeeId)
                .map(EmployeeRegistrationPersistenceAssembler::toDomain);
    }

    @Override
    public Page<EmployeeRegistration> findByBranchId(BranchId branchId, Pageable pageable) {
        return persistenceRepository.findByBranchId(branchId.value(), pageable)
                .map(EmployeeRegistrationPersistenceAssembler::toDomain);
    }

    @Override
    public Page<EmployeeRegistration> findByBranchIdAndStatus(BranchId branchId, EmployeeRegistrationStatus status, Pageable pageable) {
        return persistenceRepository.findByBranchIdAndStatus(branchId.value(), status.value(), pageable)
                .map(EmployeeRegistrationPersistenceAssembler::toDomain);
    }

    @Override
    public boolean existsByEmployeeIdAndBranchId(UUID employeeId, UUID branchId) {
        return persistenceRepository.findByEmployeeIdAndBranchId(employeeId, branchId).isPresent();
    }
}