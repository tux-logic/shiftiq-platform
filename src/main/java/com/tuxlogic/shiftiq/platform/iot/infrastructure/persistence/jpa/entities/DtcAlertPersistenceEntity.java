package com.tuxlogic.shiftiq.platform.iot.infrastructure.persistence.jpa.entities;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

/**
 * JPA entity representing the "dtc_alerts" table.
 */
@Entity
@Table(name = "dtc_alerts")
@Getter
@Setter
@NoArgsConstructor
public class DtcAlertPersistenceEntity extends AuditableAbstractPersistenceEntity implements Persistable<UUID> {

    @Column(name = "telemetry_snapshot_id", columnDefinition = "uuid", nullable = false)
    private UUID telemetrySnapshotId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "branch_id", nullable = false))
    private BranchId branchId;

    @Column(name = "dtc_code", length = 10, nullable = false)
    private String dtcCode;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(nullable = false, length = 20)
    private String severity;

    @Override
    public boolean isNew() {
        return getCreatedAt() == null;
    }
}