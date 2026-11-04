package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.assemblers;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.WorkshopSpecialty;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopSpecialtyId;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.WorkshopSpecialtyPersistenceEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WorkshopSpecialtyPersistenceAssemblerTest {

    @Test
    @DisplayName("new domain specialty with null version does not assign id or created_at to persistence entity (isNew = true)")
    void newDomainSpecialtyLeavesIdAndCreatedAtNullForPersistenceGeneration() {
        var workshopId = new WorkshopId(UUID.randomUUID());
        var domain = new WorkshopSpecialty(workshopId, "Mecánica General", "GENERAL_MECHANIC", "Servicios generales");

        var entity = new WorkshopSpecialtyPersistenceEntity();
        WorkshopSpecialtyPersistenceAssembler.toEntity(domain, entity);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getVersion()).isNull();
        assertThat(entity.isNew()).isTrue();
        assertThat(entity.getWorkshopId()).isEqualTo(workshopId.value());
        assertThat(entity.getName()).isEqualTo("Mecánica General");
        assertThat(entity.getCode()).isEqualTo("GENERAL_MECHANIC");
    }

    @Test
    @DisplayName("existing domain specialty with non-null version sets id and auditing fields (isNew = false)")
    void existingDomainSpecialtySetsIdAndAuditingFields() {
        var specialtyId = new WorkshopSpecialtyId(UUID.randomUUID());
        var workshopId = new WorkshopId(UUID.randomUUID());
        var now = Instant.now();
        var domain = new WorkshopSpecialty(specialtyId, workshopId, "Frenos", "BRAKES", "Frenos y discos", true, now, now, null, 1L);

        var entity = new WorkshopSpecialtyPersistenceEntity();
        WorkshopSpecialtyPersistenceAssembler.toEntity(domain, entity);

        assertThat(entity.getId()).isEqualTo(specialtyId.value());
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
        assertThat(entity.getVersion()).isEqualTo(1L);
        assertThat(entity.isNew()).isFalse();
    }
}
