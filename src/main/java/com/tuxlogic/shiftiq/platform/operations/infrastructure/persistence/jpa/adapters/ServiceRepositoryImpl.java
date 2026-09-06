package com.tuxlogic.shiftiq.platform.operations.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.Service;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.ServiceId;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.OperationsMessageKeys;
import com.tuxlogic.shiftiq.platform.operations.domain.repositories.ServiceRepository;
import com.tuxlogic.shiftiq.platform.operations.infrastructure.persistence.jpa.assemblers.ServicePersistenceAssembler;
import com.tuxlogic.shiftiq.platform.operations.infrastructure.persistence.jpa.entities.ServicePersistenceEntity;
import com.tuxlogic.shiftiq.platform.operations.infrastructure.persistence.jpa.repositories.ServicePersistenceRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.springframework.stereotype.Repository;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Transactional(readOnly = true)
public class ServiceRepositoryImpl implements ServiceRepository {

    private final ServicePersistenceRepository servicePersistenceRepository;

    public ServiceRepositoryImpl(ServicePersistenceRepository servicePersistenceRepository) {
        this.servicePersistenceRepository = servicePersistenceRepository;
    }

    @Override
    @Transactional
    public Service save(Service service) {
        try {
            ServicePersistenceEntity entity;
            if (service.getId() != null) {
                entity = servicePersistenceRepository.findById(service.getId().value()).orElse(new ServicePersistenceEntity());
            } else {
                entity = new ServicePersistenceEntity();
            }

            ServicePersistenceAssembler.toEntity(service, entity);
            ServicePersistenceEntity savedEntity = servicePersistenceRepository.save(entity);
            return ServicePersistenceAssembler.toDomain(savedEntity);
        } catch (Exception e) {
            throw new IllegalStateException(OperationsMessageKeys.REPOSITORY_SAVE_FAILED, e);
        }
    }

    @Override
    public Optional<Service> findById(ServiceId id) {
        return servicePersistenceRepository.findById(id.value()).map(ServicePersistenceAssembler::toDomain);
    }

    @Override
    public List<Service> findAllByBranchId(BranchId branchId) {
        return servicePersistenceRepository.findAllByBranchId(branchId.value()).stream()
                .map(ServicePersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Service service) {
        try {
            if (service.getId() != null) {
                servicePersistenceRepository.findById(service.getId().value()).ifPresent(servicePersistenceRepository::delete);
            }
        } catch (Exception e) {
            throw new IllegalStateException(OperationsMessageKeys.REPOSITORY_DELETE_FAILED, e);
        }
    }
}

