package com.tuxlogic.shiftiq.platform.core.domain.model.aggregates;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkshopSpecialtyTest {

    @Test
    @DisplayName("creates valid workshop specialty in active state")
    void createsValidSpecialty() {
        var workshopId = new WorkshopId(UUID.randomUUID());
        var specialty = new WorkshopSpecialty(workshopId, "Mecánica General", "GENERAL_MECHANIC", "Servicios generales");

        assertThat(specialty.getId()).isNotNull();
        assertThat(specialty.getWorkshopId()).isEqualTo(workshopId);
        assertThat(specialty.getName()).isEqualTo("Mecánica General");
        assertThat(specialty.getCode()).isEqualTo("GENERAL_MECHANIC");
        assertThat(specialty.getDescription()).isEqualTo("Servicios generales");
        assertThat(specialty.isActive()).isTrue();
        assertThat(specialty.getCreatedAt()).isNotNull();
        assertThat(specialty.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("validates required name and code")
    void validatesRequiredFields() {
        var workshopId = new WorkshopId(UUID.randomUUID());

        assertThatThrownBy(() -> new WorkshopSpecialty(workshopId, "", "CODE", null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new WorkshopSpecialty(workshopId, "Name", "  ", null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new WorkshopSpecialty(null, "Name", "CODE", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("updates name and description successfully")
    void updatesSpecialty() {
        var workshopId = new WorkshopId(UUID.randomUUID());
        var specialty = new WorkshopSpecialty(workshopId, "Mecánica", "MEC", "Desc");

        specialty.update("Mecánica Avanzada", "Nueva desc");

        assertThat(specialty.getName()).isEqualTo("Mecánica Avanzada");
        assertThat(specialty.getDescription()).isEqualTo("Nueva desc");
        assertThat(specialty.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("deactivates and reactivates correctly")
    void deactivatesAndReactivates() {
        var workshopId = new WorkshopId(UUID.randomUUID());
        var specialty = new WorkshopSpecialty(workshopId, "Pintura", "PAINT", null);

        specialty.deactivate();
        assertThat(specialty.isActive()).isFalse();
        assertThat(specialty.getDeletedAt()).isNotNull();

        specialty.activate();
        assertThat(specialty.isActive()).isTrue();
        assertThat(specialty.getDeletedAt()).isNull();
    }
}
