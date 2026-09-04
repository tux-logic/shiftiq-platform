package com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.entities.CustomerRegistrationPersistenceEntity;
import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.repositories.CustomerRegistrationPersistenceRepository;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.CustomerRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.CustomerRegistrationRepository;
import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.assemblers.CustomerRegistrationPersistenceAssembler;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<CustomerRegistration> findByBranchIdAndStatus(BranchId branchId, String status, Pageable pageable) {
        return persistenceRepository.findByBranchIdAndStatus(branchId.value(), status, pageable).map(CustomerRegistrationPersistenceAssembler::toDomain);
    }

    @Override
    public boolean existsByCustomerIdAndBranchId(UUID customerId, UUID branchId) {
        return persistenceRepository.findByCustomerIdAndBranchId(customerId, branchId).isPresent();
    }
}







