package com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.assemblers;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.Appointment;
import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.entities.AppointmentPersistenceEntity;

public class AppointmentPersistenceAssembler {

    public static AppointmentPersistenceEntity toEntityFromAggregate(Appointment aggregate) {
        return toEntityFromAggregate(aggregate, new AppointmentPersistenceEntity());
    }

    public static AppointmentPersistenceEntity toEntityFromAggregate(Appointment aggregate, AppointmentPersistenceEntity targetEntity) {
        if (targetEntity == null) {
            targetEntity = new AppointmentPersistenceEntity();
        }

        if (aggregate.getId() != null) {
            targetEntity.setId(aggregate.getId());
        }
        if (aggregate.getVersion() != null) {
            targetEntity.setVersion(aggregate.getVersion());
        }
        targetEntity.setBranchId(aggregate.getBranchId());
        targetEntity.setCustomerId(aggregate.getCustomerId());
        targetEntity.setVehicleId(aggregate.getVehicleId());
        targetEntity.setStatus(aggregate.getStatus());
        targetEntity.setScheduledStart(aggregate.getScheduledStart());
        targetEntity.setScheduledEnd(aggregate.getScheduledEnd());
        targetEntity.setNotes(aggregate.getNotes());
        targetEntity.setDeletedAt(aggregate.getDeletedAt());
        targetEntity.setCreatedBy(aggregate.getCreatedBy());
        targetEntity.setUpdatedBy(aggregate.getUpdatedBy());

        if (aggregate.getCreatedAt() != null) {
            targetEntity.setCreatedAt(aggregate.getCreatedAt());
        }

        if (aggregate.getUpdatedAt() != null) {
            targetEntity.setUpdatedAt(aggregate.getUpdatedAt());
        }

        return targetEntity;
    }


    public static Appointment toAggregateFromEntity(AppointmentPersistenceEntity entity) {
        return new Appointment(
                entity.getId(),
                entity.getBranchId(),
                entity.getCustomerId(),
                entity.getVehicleId(),
                entity.getScheduledStart(),
                entity.getScheduledEnd(),
                entity.getStatus(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedBy(),
                entity.getVersion()
        );
    }
}