package com.tuxlogic.shiftiq.platform.core.domain.model.aggregates;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopSpecialtyId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class WorkshopSpecialty extends AbstractDomainAggregateRoot<WorkshopSpecialty> {

    private WorkshopSpecialtyId id;
    private WorkshopId workshopId;
    private String name;
    private String code;
    private String description;
    private boolean active = true;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private Long version;

    public WorkshopSpecialty() {}

    public WorkshopSpecialty(WorkshopSpecialtyId id, WorkshopId workshopId, String name, String code,
                             String description, boolean active, Instant createdAt, Instant updatedAt,
                             Instant deletedAt, Long version) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("core.error.specialtyName.required");
        if (code == null || code.isBlank()) throw new IllegalArgumentException("core.error.specialtyCode.required");
        this.id = id;
        this.workshopId = workshopId;
        this.name = name.trim();
        this.code = code.trim().toUpperCase();
        this.description = description;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.version = version;
    }

    public WorkshopSpecialty(WorkshopId workshopId, String name, String code, String description) {
        if (workshopId == null) throw new IllegalArgumentException("core.error.workshopId.required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("core.error.specialtyName.required");
        if (code == null || code.isBlank()) throw new IllegalArgumentException("core.error.specialtyCode.required");
        this.id = new WorkshopSpecialtyId(UUID.randomUUID());
        this.workshopId = workshopId;
        this.name = name.trim();
        this.code = code.trim().toUpperCase();
        this.description = description;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void update(String name, String description) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("core.error.specialtyName.required");
        this.name = name.trim();
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
        this.deletedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.active = true;
        this.deletedAt = null;
        this.updatedAt = Instant.now();
    }
}
