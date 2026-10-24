package com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.assemblers;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.entities.StaffCareerSnapshot;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.EmployeeRegistrationStatus;
import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.entities.StaffCareerHistoryPersistenceEntity;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

public class StaffCareerHistoryPersistenceAssembler {

    public static StaffCareerHistoryPersistenceEntity toEntity(StaffCareerSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        return new StaffCareerHistoryPersistenceEntity(
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

    public static StaffCareerSnapshot toDomain(StaffCareerHistoryPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new StaffCareerSnapshot(
                entity.getId(),
                entity.getRegistrationId(),
                entity.getEmployeeId(),
                entity.getBranchId() != null ? new BranchId(entity.getBranchId()) : null,
                entity.getSpeciality(),
                entity.getSpecialityName(),
                entity.getSalary(),
                entity.getStatus() != null ? new EmployeeRegistrationStatus(entity.getStatus()) : null,
                entity.getChangeType(),
                entity.getChangeReason(),
                entity.getChangedBy(),
                entity.getRecordedAt()
        );
    }
}
