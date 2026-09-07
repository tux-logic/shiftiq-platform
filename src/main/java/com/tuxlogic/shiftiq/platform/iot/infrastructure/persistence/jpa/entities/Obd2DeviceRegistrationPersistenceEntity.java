package com.tuxlogic.shiftiq.platform.iot.infrastructure.persistence.jpa.entities;

import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.Obd2DeviceId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing the "obd2_device_registrations" table.
 */
@Entity
@Table(name = "obd2_device_registrations")
@SQLDelete(sql = "UPDATE obd2_device_registrations SET deleted_at = CURRENT_TIMESTAMP, status = 'INACTIVE' WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class Obd2DeviceRegistrationPersistenceEntity extends AuditableAbstractPersistenceEntity implements Persistable<UUID> {

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "obd2_device_id", nullable = false))
    private Obd2DeviceId obd2DeviceId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "branch_id", nullable = false))
    private BranchId branchId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "vehicle_id", nullable = false))
    private VehicleId vehicleId;

    @Column(nullable = false)
    private String status;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Override
    public boolean isNew() {
        return getCreatedAt() == null;
    }
}