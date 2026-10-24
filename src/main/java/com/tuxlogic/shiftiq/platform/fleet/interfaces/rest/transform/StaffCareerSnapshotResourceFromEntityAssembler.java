package com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.entities.StaffCareerSnapshot;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.resources.StaffCareerSnapshotResource;

public class StaffCareerSnapshotResourceFromEntityAssembler {

    public static StaffCareerSnapshotResource toResource(StaffCareerSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        return new StaffCareerSnapshotResource(
                snapshot.id(),
                snapshot.registrationId(),
                snapshot.employeeId(),
                snapshot.branchId() != null ? snapshot.branchId().value() : null,
                snapshot.speciality(),
                snapshot.specialityName(),
                snapshot.salary(),
                snapshot.status() != null ? snapshot.status().value() : null,
                snapshot.changeType(),
                snapshot.changeReason(),
                snapshot.changedBy(),
                snapshot.recordedAt()
        );
    }
}
