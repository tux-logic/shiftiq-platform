package com.tuxlogic.shiftiq.platform.iot.infrastructure.persistence.jpa.entities;

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
 * JPA entity representing the "vehicle_registrations" table.
 */
@Entity
@Table(name = "vehicle_registrations")
@SQLDelete(sql = "UPDATE vehicle_registrations SET deleted_at = CURRENT_TIMESTAMP, status = 'PREVIOUS' WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class VehicleRegistrationPersistenceEntity extends AuditableAbstractPersistenceEntity implements Persistable<UUID> {

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
    private UUID userId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "vehicle_id", nullable = false))
    private VehicleId vehicleId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Override
    public boolean isNew() {
        return getCreatedAt() == null;
    }
}