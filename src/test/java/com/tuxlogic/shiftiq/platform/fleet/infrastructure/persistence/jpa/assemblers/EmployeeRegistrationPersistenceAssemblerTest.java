package com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.assemblers;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.EmployeeRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.EmployeeRegistrationStatus;
import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.entities.EmployeeRegistrationPersistenceEntity;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeRegistrationPersistenceAssemblerTest {

    @Test
    @DisplayName("new domain employee registration with null version leaves id and created_at null for persistence generation (isNew = true)")
    void newRegistrationLeavesIdAndCreatedAtNull() {
        var employeeId = UUID.randomUUID();
        var branchId = new BranchId(UUID.randomUUID());
        var domain = new EmployeeRegistration(employeeId, branchId, "GENERAL_MECHANIC", "Mecánica General", new BigDecimal("2500"), EmployeeRegistrationStatus.PENDING_APPROVAL);

        var entity = new EmployeeRegistrationPersistenceEntity();
        EmployeeRegistrationPersistenceAssembler.toEntity(domain, entity);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getVersion()).isNull();
        assertThat(entity.isNew()).isTrue();
        assertThat(entity.getEmployeeId()).isEqualTo(employeeId);
        assertThat(entity.getBranchId()).isEqualTo(branchId.value());
        assertThat(entity.getStatus()).isEqualTo("PENDING_APPROVAL");
    }

    @Test
    @DisplayName("existing domain employee registration with non-null version sets id and auditing fields (isNew = false)")
    void existingRegistrationSetsIdAndAuditingFields() {
        var registrationId = new EmployeeId(UUID.randomUUID());
        var employeeId = UUID.randomUUID();
        var branchId = new BranchId(UUID.randomUUID());
        var now = Instant.now();
        var domain = new EmployeeRegistration(registrationId, employeeId, branchId, "GENERAL_MECHANIC", "Mecánica General", new BigDecimal("2500"), EmployeeRegistrationStatus.ACTIVE, now, now, null, 1L);

        var entity = new EmployeeRegistrationPersistenceEntity();
        EmployeeRegistrationPersistenceAssembler.toEntity(domain, entity);

        assertThat(entity.getId()).isEqualTo(registrationId.value());
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
        assertThat(entity.getVersion()).isEqualTo(1L);
        assertThat(entity.isNew()).isFalse();
    }
}
