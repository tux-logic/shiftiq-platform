package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderId;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderPersistenceEntity;

import java.util.stream.Collectors;
import java.util.Collections;

public final class WorkOrderPersistenceAssembler {

    public WorkOrderPersistenceAssembler() {}

    public static WorkOrderPersistenceEntity toPersistenceEntity(WorkOrder domainEntity) {
        if (domainEntity == null) return null;
        WorkOrderPersistenceEntity persistenceEntity = new WorkOrderPersistenceEntity();
        if (domainEntity.getVersion() != null) {
            persistenceEntity.setId(domainEntity.getId() != null ? domainEntity.getId().value() : null);
        }
        persistenceEntity.setAppointmentId(domainEntity.getAppointmentId());
        persistenceEntity.setBranchId(domainEntity.getBranchId());
        persistenceEntity.setVehicleId(domainEntity.getVehicleId());
        persistenceEntity.setCustomerId(domainEntity.getCustomerId());
        persistenceEntity.setInternalNumber(domainEntity.getInternalNumber());
        persistenceEntity.setStatus(domainEntity.getStatus());
        persistenceEntity.setDiagnosticSummary(domainEntity.getDiagnosticSummary());
        persistenceEntity.setMileageIn(domainEntity.getMileageIn());
        persistenceEntity.setTotalAmount(domainEntity.getTotalAmount());
        persistenceEntity.setTasks(domainEntity.getTasks() != null ? domainEntity.getTasks().stream()
                .map(WorkOrderTaskPersistenceAssembler::toPersistenceEntity)
                .collect(Collectors.toList()) : Collections.emptyList());
        persistenceEntity.setCreatedAt(domainEntity.getCreatedAt());
        persistenceEntity.setUpdatedAt(domainEntity.getUpdatedAt());
        persistenceEntity.setCreatedBy(domainEntity.getCreatedBy());
        persistenceEntity.setUpdatedBy(domainEntity.getUpdatedBy());
        persistenceEntity.setDeletedAt(domainEntity.getDeletedAt());
        persistenceEntity.setVersion(domainEntity.getVersion());
        return persistenceEntity;
    }

    public static WorkOrder toDomainEntity(WorkOrderPersistenceEntity persistenceEntity) {
        if (persistenceEntity == null) return null;
        WorkOrder workOrder = new WorkOrder(
                new WorkOrderId(persistenceEntity.getId()),
                persistenceEntity.getAppointmentId(),
                persistenceEntity.getBranchId(),
                persistenceEntity.getVehicleId(),
                persistenceEntity.getCustomerId(),
                persistenceEntity.getInternalNumber(),
                persistenceEntity.getStatus(),
                persistenceEntity.getDiagnosticSummary(),
                persistenceEntity.getMileageIn(),
                persistenceEntity.getTotalAmount(),
                persistenceEntity.getTasks() != null ? persistenceEntity.getTasks().stream()
                        .map(WorkOrderTaskPersistenceAssembler::toDomainEntity)
                        .collect(Collectors.toList()) : Collections.emptyList(),
                persistenceEntity.getCreatedAt(),
                persistenceEntity.getUpdatedAt(),
                persistenceEntity.getDeletedAt(),
                persistenceEntity.getCreatedBy(),
                persistenceEntity.getUpdatedBy(),
                persistenceEntity.getVersion()
        );
        // Important: in case we need to keep tracking of domain events that were registered
        // but normally assemblers are used for loading from DB where no unsaved events exist yet.
        return workOrder;
    }
}
