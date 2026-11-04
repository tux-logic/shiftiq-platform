package com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.assemblers;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;
import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.entities.EmployeeRegistrationPersistenceEntity;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.EmployeeRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.EmployeeRegistrationStatus;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

public class EmployeeRegistrationPersistenceAssembler {

    public static EmployeeRegistration toDomain(EmployeeRegistrationPersistenceEntity entity) {
        if (entity == null) return null;
        return new EmployeeRegistration(
                new EmployeeId(entity.getId()),
                entity.getEmployeeId(),
                new BranchId(entity.getBranchId()),
                entity.getSpeciality(),
                entity.getSpecialityName(),
                entity.getSalary(),
                entity.getStatus() != null ? new EmployeeRegistrationStatus(entity.getStatus()) : null,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getVersion()
        );
    }

    public static EmployeeRegistrationPersistenceEntity toEntity(EmployeeRegistration domain, EmployeeRegistrationPersistenceEntity entity) {
        if (domain == null) return null;
        if (entity == null) entity = new EmployeeRegistrationPersistenceEntity();
        if (domain.getVersion() != null) {
            entity.setId(domain.getId() != null ? domain.getId().value() : null);
            entity.setCreatedAt(domain.getCreatedAt());
            entity.setUpdatedAt(domain.getUpdatedAt());
            entity.setVersion(domain.getVersion());
        }
        entity.setEmployeeId(domain.getEmployeeId());
        entity.setBranchId(domain.getBranchId() != null ? domain.getBranchId().value() : null);
        entity.setSpeciality(domain.getSpeciality());
        entity.setSpecialityName(domain.getSpecialityName());
        entity.setSalary(domain.getSalary());
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().value() : null);
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }
}
