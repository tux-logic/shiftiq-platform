package com.tuxlogic.shiftiq.platform.operations.infrastructure.persistence.jpa.assemblers;

import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.WorkOrder;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderId;
import com.tuxlogic.shiftiq.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderPersistenceEntity;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Collections;

public final class WorkOrderPersistenceAssembler {

    public WorkOrderPersistenceAssembler() {}

    public static WorkOrderPersistenceEntity toPersistenceEntity(WorkOrder domainEntity, WorkOrderPersistenceEntity persistenceEntity) {
        if (domainEntity == null) return null;
        if (persistenceEntity == null) persistenceEntity = new WorkOrderPersistenceEntity();
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
        persistenceEntity.setEntryInspectionImages(domainEntity.getEntryInspectionImages() != null ? new ArrayList<>(domainEntity.getEntryInspectionImages()) : new ArrayList<>());
        persistenceEntity.setCreatedAt(domainEntity.getCreatedAt());
        persistenceEntity.setUpdatedAt(domainEntity.getUpdatedAt());
        persistenceEntity.setCreatedBy(domainEntity.getCreatedBy());
        persistenceEntity.setUpdatedBy(domainEntity.getUpdatedBy());
        persistenceEntity.setDeletedAt(domainEntity.getDeletedAt());
        persistenceEntity.setVersion(domainEntity.getVersion());

        // Synchronize tasks
        if (persistenceEntity.getTasks() == null) {
            persistenceEntity.setTasks(new ArrayList<>());
        }
        if (domainEntity.getTasks() != null) {
            var domainTaskIds = domainEntity.getTasks().stream()
                    .map(t -> t.getId() != null ? t.getId().value() : null)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            persistenceEntity.getTasks().removeIf(te -> te.getId() != null && !domainTaskIds.contains(te.getId()));

            for (var dt : domainEntity.getTasks()) {
                var existingTe = persistenceEntity.getTasks().stream()
                        .filter(te -> te.getId() != null && dt.getId() != null && te.getId().equals(dt.getId().value()))
                        .findFirst()
                        .orElse(null);
                if (existingTe == null) {
                    existingTe = new com.tuxlogic.shiftiq.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderTaskPersistenceEntity();
                    persistenceEntity.getTasks().add(existingTe);
                }
                WorkOrderTaskPersistenceAssembler.toPersistenceEntity(dt, persistenceEntity, existingTe);
            }
        } else {
            persistenceEntity.getTasks().clear();
        }

        return persistenceEntity;
    }

    public static WorkOrderPersistenceEntity toPersistenceEntity(WorkOrder domainEntity) {
        return toPersistenceEntity(domainEntity, new WorkOrderPersistenceEntity());
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
                persistenceEntity.getEntryInspectionImages() != null ? new ArrayList<>(persistenceEntity.getEntryInspectionImages()) : new ArrayList<>(),
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
