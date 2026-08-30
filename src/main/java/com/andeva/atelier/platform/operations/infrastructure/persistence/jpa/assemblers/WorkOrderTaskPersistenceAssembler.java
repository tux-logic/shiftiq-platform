package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderTaskPersistenceEntity;

import java.util.stream.Collectors;
import java.util.Collections;

public final class WorkOrderTaskPersistenceAssembler {

    public WorkOrderTaskPersistenceAssembler() {}

    public static WorkOrderTaskPersistenceEntity toPersistenceEntity(WorkOrderTask domainEntity) {
        if (domainEntity == null) return null;
        WorkOrderTaskPersistenceEntity persistenceEntity = new WorkOrderTaskPersistenceEntity();
        if (domainEntity.getVersion() != null) {
            persistenceEntity.setId(domainEntity.getId() != null ? domainEntity.getId().value() : null);
        }
        persistenceEntity.setServiceId(domainEntity.getServiceId());
        persistenceEntity.setBranchId(domainEntity.getBranchId());
        persistenceEntity.setAssignedMechanicId(domainEntity.getAssignedMechanicId());
        persistenceEntity.setStatus(domainEntity.getStatus());
        persistenceEntity.setDescription(domainEntity.getDescription());
        persistenceEntity.setPrice(domainEntity.getPrice());
        persistenceEntity.setStartedAt(domainEntity.getStartedAt());
        persistenceEntity.setCompletedAt(domainEntity.getCompletedAt());
        persistenceEntity.setProducts(domainEntity.getProducts() != null ? domainEntity.getProducts().stream()
                .map(WorkOrderTaskProductPersistenceAssembler::toPersistenceEntity)
                .collect(Collectors.toList()) : Collections.emptyList());
        persistenceEntity.setCreatedAt(domainEntity.getCreatedAt());
        persistenceEntity.setUpdatedAt(domainEntity.getUpdatedAt());
        persistenceEntity.setCreatedBy(domainEntity.getCreatedBy());
        persistenceEntity.setUpdatedBy(domainEntity.getUpdatedBy());
        persistenceEntity.setDeletedAt(domainEntity.getDeletedAt());
        persistenceEntity.setVersion(domainEntity.getVersion());
        return persistenceEntity;
    }

    public static WorkOrderTask toDomainEntity(WorkOrderTaskPersistenceEntity persistenceEntity) {
        if (persistenceEntity == null) return null;
        return new WorkOrderTask(
                new WorkOrderTaskId(persistenceEntity.getId()),
                persistenceEntity.getServiceId(),
                persistenceEntity.getBranchId(),
                persistenceEntity.getAssignedMechanicId(),
                persistenceEntity.getStatus(),
                persistenceEntity.getDescription(),
                persistenceEntity.getPrice(),
                persistenceEntity.getStartedAt(),
                persistenceEntity.getCompletedAt(),
                persistenceEntity.getProducts() != null ? persistenceEntity.getProducts().stream()
                        .map(WorkOrderTaskProductPersistenceAssembler::toDomainEntity)
                        .collect(Collectors.toList()) : Collections.emptyList(),
                persistenceEntity.getCreatedAt(),
                persistenceEntity.getUpdatedAt(),
                persistenceEntity.getDeletedAt(),
                persistenceEntity.getCreatedBy(),
                persistenceEntity.getUpdatedBy(),
                persistenceEntity.getVersion()
        );
    }
}
