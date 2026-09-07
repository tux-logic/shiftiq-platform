package com.tuxlogic.shiftiq.platform.iot.infrastructure.persistence.jpa.assemblers;

import com.tuxlogic.shiftiq.platform.iot.domain.model.aggregates.DtcAlert;
import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.DtcAlertId;
import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.DtcAlertSeverity;
import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.TelemetrySnapshotId;
import com.tuxlogic.shiftiq.platform.iot.infrastructure.persistence.jpa.entities.DtcAlertPersistenceEntity;

/**
 * Assembler to translate between DtcAlert persistence entity and domain aggregate.
 */
public class DtcAlertPersistenceAssembler {

    public static DtcAlertPersistenceEntity toPersistenceEntity(DtcAlert domain) {
        if (domain == null) {
            return null;
        }
        DtcAlertPersistenceEntity entity = new DtcAlertPersistenceEntity();
        entity.setId(domain.getId() != null ? domain.getId().value() : null);
        entity.setTelemetrySnapshotId(domain.getTelemetrySnapshotId().value());
        entity.setBranchId(domain.getBranchId());
        entity.setDtcCode(domain.getDtcCode());
        entity.setDescription(domain.getDescription());
        entity.setSeverity(domain.getSeverity() != null ? domain.getSeverity().value() : null);
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    public static DtcAlert toDomainEntity(DtcAlertPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new DtcAlert(
                new DtcAlertId(entity.getId()),
                new TelemetrySnapshotId(entity.getTelemetrySnapshotId()),
                entity.getBranchId(),
                entity.getDtcCode(),
                entity.getDescription(),
                entity.getSeverity() != null ? new DtcAlertSeverity(entity.getSeverity()) : null,
                entity.getCreatedAt()
        );
    }
}