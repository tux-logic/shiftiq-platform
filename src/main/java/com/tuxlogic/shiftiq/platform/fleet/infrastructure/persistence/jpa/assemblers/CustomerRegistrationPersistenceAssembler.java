package com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.assemblers;

import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.entities.CustomerRegistrationPersistenceEntity;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.CustomerRegistration;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.CustomerRegistrationStatus;

public class CustomerRegistrationPersistenceAssembler {

    public static CustomerRegistration toDomain(CustomerRegistrationPersistenceEntity entity) {
        if (entity == null) return null;
        return new CustomerRegistration(
                new CustomerId(entity.getId()),
                entity.getCustomerId(),
                new BranchId(entity.getBranchId()),
                entity.getStatus() != null ? new CustomerRegistrationStatus(entity.getStatus()) : null,
                entity.getCreatedAt(),
                entity.getDeletedAt()
        );
    }

    public static CustomerRegistrationPersistenceEntity toEntity(CustomerRegistration domain, CustomerRegistrationPersistenceEntity entity) {
        if (domain == null) return null;
        if (entity == null) entity = new CustomerRegistrationPersistenceEntity();
        // map fields
        if (entity.getId() != null) {
            entity.setId(domain.getId() != null ? domain.getId().value() : null);
        }
        entity.setCustomerId(domain.getCustomerId());
        entity.setBranchId(domain.getBranchId() != null ? domain.getBranchId().value() : null);
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().value() : null);
        if (entity.getCreatedAt() != null) {
            entity.setCreatedAt(domain.getCreatedAt());
        }
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }
}

