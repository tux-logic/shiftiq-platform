package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.assemblers;

import com.tuxlogic.shiftiq.platform.core.domain.model.entities.WorkshopSpecialtyRevision;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopSpecialtyId;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.WorkshopSpecialtyHistoryPersistenceEntity;

public class WorkshopSpecialtyHistoryPersistenceAssembler {

    public static WorkshopSpecialtyHistoryPersistenceEntity toEntity(WorkshopSpecialtyRevision revision) {
        if (revision == null) {
            return null;
        }
        return new WorkshopSpecialtyHistoryPersistenceEntity(
                revision.id(),
                revision.specialtyId() != null ? revision.specialtyId().value() : null,
                revision.workshopId() != null ? revision.workshopId().value() : null,
                revision.name(),
                revision.code(),
                revision.description(),
                revision.active(),
                revision.changeType(),
                revision.changedBy(),
                revision.recordedAt()
        );
    }

    public static WorkshopSpecialtyRevision toDomain(WorkshopSpecialtyHistoryPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new WorkshopSpecialtyRevision(
                entity.getId(),
                entity.getSpecialtyId() != null ? new WorkshopSpecialtyId(entity.getSpecialtyId()) : null,
                entity.getWorkshopId() != null ? new WorkshopId(entity.getWorkshopId()) : null,
                entity.getName(),
                entity.getCode(),
                entity.getDescription(),
                entity.isActive(),
                entity.getChangeType(),
                entity.getChangedBy(),
                entity.getRecordedAt()
        );
    }
}
