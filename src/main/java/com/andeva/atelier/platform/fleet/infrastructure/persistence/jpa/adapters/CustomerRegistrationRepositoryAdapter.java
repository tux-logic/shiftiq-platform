package com.andeva.atelier.platform.fleet.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.fleet.infrastructure.persistence.jpa.entities.CustomerRegistrationPersistenceEntity;
import com.andeva.atelier.platform.fleet.infrastructure.persistence.jpa.repositories.CustomerRegistrationPersistenceRepository;
import com.andeva.atelier.platform.fleet.domain.model.aggregates.CustomerRegistration;
import com.andeva.atelier.platform.fleet.domain.repositories.CustomerRegistrationRepository;
import com.andeva.atelier.platform.fleet.infrastructure.persistence.jpa.assemblers.CustomerRegistrationPersistenceAssembler;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class CustomerRegistrationRepositoryAdapter implements CustomerRegistrationRepository {

    private final CustomerRegistrationPersistenceRepository persistenceRepository;

    public CustomerRegistrationRepositoryAdapter(CustomerRegistrationPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public CustomerRegistration save(CustomerRegistration registration) {
        CustomerRegistrationPersistenceEntity entity;
        if (registration.getId() != null) {
            Optional<CustomerRegistrationPersistenceEntity> opt = persistenceRepository.findById(registration.getId().value());
            entity = opt.orElseGet(CustomerRegistrationPersistenceEntity::new);
        } else {
            entity = new CustomerRegistrationPersistenceEntity();
        }
        CustomerRegistrationPersistenceAssembler.toEntity(registration, entity);
        CustomerRegistrationPersistenceEntity saved = persistenceRepository.save(entity);
        return CustomerRegistrationPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<CustomerRegistration> findById(UUID id) {
        return persistenceRepository.findById(id).map(CustomerRegistrationPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<CustomerRegistration> findByCustomerId(UUID customerId) {
        return persistenceRepository.findByCustomerId(customerId)
                .stream()
                .findFirst()
                .map(CustomerRegistrationPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<CustomerRegistration> findByCustomerIdAndBranchId(UUID customerId, UUID branchId) {
        return persistenceRepository.findByCustomerIdAndBranchId(customerId, branchId).map(CustomerRegistrationPersistenceAssembler::toDomain);
    }

    @Override
    public List<CustomerRegistration> findByBranchIdAndStatus(BranchId branchId, String status) {
        return persistenceRepository.findByBranchIdAndStatus(branchId.value(), status)
                .stream()
                .map(CustomerRegistrationPersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByCustomerIdAndBranchId(UUID customerId, UUID branchId) {
        return persistenceRepository.findByCustomerIdAndBranchId(customerId, branchId).isPresent();
    }
}

