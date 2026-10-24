package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workshop_specialty_history")
@Getter
@Setter
@NoArgsConstructor
public class WorkshopSpecialtyHistoryPersistenceEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "specialty_id", nullable = false)
    private UUID specialtyId;

    @Column(name = "workshop_id", nullable = false)
    private UUID workshopId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "change_type", nullable = false, length = 50)
    private String changeType;

    @Column(name = "changed_by")
    private UUID changedBy;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    public WorkshopSpecialtyHistoryPersistenceEntity(UUID id, UUID specialtyId, UUID workshopId,
                                                     String name, String code, String description,
                                                     boolean active, String changeType,
                                                     UUID changedBy, Instant recordedAt) {
        this.id = id;
        this.specialtyId = specialtyId;
        this.workshopId = workshopId;
        this.name = name;
        this.code = code;
        this.description = description;
        this.active = active;
        this.changeType = changeType;
        this.changedBy = changedBy;
        this.recordedAt = recordedAt;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
