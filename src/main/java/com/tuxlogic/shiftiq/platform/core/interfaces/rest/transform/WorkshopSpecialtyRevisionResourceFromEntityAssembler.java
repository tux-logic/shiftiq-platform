package com.tuxlogic.shiftiq.platform.core.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.core.domain.model.entities.WorkshopSpecialtyRevision;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.WorkshopSpecialtyRevisionResource;

public class WorkshopSpecialtyRevisionResourceFromEntityAssembler {

    public static WorkshopSpecialtyRevisionResource toResource(WorkshopSpecialtyRevision revision) {
        if (revision == null) {
            return null;
        }
        return new WorkshopSpecialtyRevisionResource(
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
}
