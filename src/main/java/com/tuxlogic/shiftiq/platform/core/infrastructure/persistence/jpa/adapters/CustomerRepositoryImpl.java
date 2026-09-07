package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Customer;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.UserId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.CustomerRepository;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.assemblers.CustomerPersistenceAssembler;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.CustomerPersistenceEntity;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories.CustomerPersistenceRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class CustomerRepositoryImpl implements CustomerRepository {

    private final CustomerPersistenceRepository customerPersistenceRepository;

    public CustomerRepositoryImpl(CustomerPersistenceRepository customerPersistenceRepository) {
        this.customerPersistenceRepository = customerPersistenceRepository;
    }

    @Override
    @Transactional
    public Customer save(Customer customer) {
        var entity = JpaAdapterUtils.resolveEntity(
                customer.getId() != null ? customer.getId().value() : null,
                customerPersistenceRepository::findById,
                CustomerPersistenceEntity::new
        );
        CustomerPersistenceAssembler.toEntity(customer, entity);
        return CustomerPersistenceAssembler.toDomain(customerPersistenceRepository.save(entity));
    }

    @Override
    public Optional<Customer> findById(CustomerId id) {
        return customerPersistenceRepository.findById(id.value()).map(CustomerPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<Customer> findByUserId(UserId userId) {
        return customerPersistenceRepository.findByUserId(userId.value()).map(CustomerPersistenceAssembler::toDomain);
    }

    @Override
    public boolean existsByUserId(UserId userId) {
        return customerPersistenceRepository.existsByUserId(userId.value());
    }

    @Override
    public Optional<Customer> findByDocumentNumber(String documentNumber) {
        return customerPersistenceRepository.findByDocumentNumber(documentNumber).map(CustomerPersistenceAssembler::toDomain);
    }

    @Override
    public java.util.List<String> findProfileRolesByUserId(UserId userId) {
        if (userId == null || userId.value() == null) {
            return java.util.Collections.emptyList();
        }
        return customerPersistenceRepository.findProfileRolesByUserId(userId.value());
    }

    @Override
    @Transactional
    public void delete(Customer customer) {
        if (customer == null || customer.getId() == null) {
            throw new IllegalArgumentException("Customer or Customer ID cannot be null for deletion");
        }
        customerPersistenceRepository.deleteById(customer.getId().value());
    }
}

