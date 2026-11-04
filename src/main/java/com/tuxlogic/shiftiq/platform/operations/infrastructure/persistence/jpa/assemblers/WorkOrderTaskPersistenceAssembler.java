package com.tuxlogic.shiftiq.platform.operations.infrastructure.persistence.jpa.assemblers;

import com.tuxlogic.shiftiq.platform.operations.domain.model.entities.WorkOrderTask;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderTaskId;
import com.tuxlogic.shiftiq.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderTaskPersistenceEntity;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Collections;

public final class WorkOrderTaskPersistenceAssembler {

    public WorkOrderTaskPersistenceAssembler() {}

    public static WorkOrderTaskPersistenceEntity toPersistenceEntity(WorkOrderTask domainEntity,
                                                                     com.tuxlogic.shiftiq.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderPersistenceEntity workOrderEntity,
                                                                     WorkOrderTaskPersistenceEntity persistenceEntity) {
        if (domainEntity == null) return null;
        if (persistenceEntity == null) persistenceEntity = new WorkOrderTaskPersistenceEntity();
        if (domainEntity.getVersion() != null) {
            persistenceEntity.setId(domainEntity.getId() != null ? domainEntity.getId().value() : null);
        }
        persistenceEntity.setWorkOrder(workOrderEntity);
        persistenceEntity.setServiceId(domainEntity.getServiceId());
        persistenceEntity.setBranchId(domainEntity.getBranchId());
        persistenceEntity.setAssignedMechanicId(domainEntity.getAssignedMechanicId());
        persistenceEntity.setStatus(domainEntity.getStatus());
        persistenceEntity.setDescription(domainEntity.getDescription());
        persistenceEntity.setPrice(domainEntity.getPrice());
        persistenceEntity.setStartedAt(domainEntity.getStartedAt());
        persistenceEntity.setCompletedAt(domainEntity.getCompletedAt());
        persistenceEntity.setEvidenceImages(domainEntity.getEvidenceImages() != null ? new ArrayList<>(domainEntity.getEvidenceImages()) : new ArrayList<>());
        persistenceEntity.setCreatedAt(domainEntity.getCreatedAt());
        persistenceEntity.setUpdatedAt(domainEntity.getUpdatedAt());
        persistenceEntity.setCreatedBy(domainEntity.getCreatedBy());
        persistenceEntity.setUpdatedBy(domainEntity.getUpdatedBy());
        persistenceEntity.setDeletedAt(domainEntity.getDeletedAt());
        persistenceEntity.setVersion(domainEntity.getVersion());

        // Synchronize products
        if (persistenceEntity.getProducts() == null) {
            persistenceEntity.setProducts(new ArrayList<>());
        }
        if (domainEntity.getProducts() != null) {
            var domainProductIds = domainEntity.getProducts().stream()
                    .map(p -> p.getId() != null ? p.getId().value() : null)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            persistenceEntity.getProducts().removeIf(pe -> pe.getId() != null && !domainProductIds.contains(pe.getId()));

            for (var dp : domainEntity.getProducts()) {
                var existingPe = persistenceEntity.getProducts().stream()
                        .filter(pe -> pe.getId() != null && dp.getId() != null && pe.getId().equals(dp.getId().value()))
                        .findFirst()
                        .orElse(null);
                if (existingPe == null) {
                    existingPe = new com.tuxlogic.shiftiq.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderTaskProductPersistenceEntity();
                    persistenceEntity.getProducts().add(existingPe);
                }
                WorkOrderTaskProductPersistenceAssembler.toPersistenceEntity(dp, persistenceEntity, existingPe);
            }
        } else {
            persistenceEntity.getProducts().clear();
        }

        return persistenceEntity;
    }

    public static WorkOrderTaskPersistenceEntity toPersistenceEntity(WorkOrderTask domainEntity) {
        return toPersistenceEntity(domainEntity, null, new WorkOrderTaskPersistenceEntity());
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
                persistenceEntity.getEvidenceImages() != null ? new ArrayList<>(persistenceEntity.getEvidenceImages()) : new ArrayList<>(),
                persistenceEntity.getCreatedAt(),
                persistenceEntity.getUpdatedAt(),
                persistenceEntity.getDeletedAt(),
                persistenceEntity.getCreatedBy(),
                persistenceEntity.getUpdatedBy(),
                persistenceEntity.getVersion()
        );
    }
}
