package com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.assemblers;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.CustomerRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.CustomerRegistrationStatus;
import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.entities.CustomerRegistrationPersistenceEntity;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerRegistrationPersistenceAssemblerTest {

    @Test
    @DisplayName("new domain customer registration with null version leaves id and created_at null for persistence generation (isNew = true)")
    void newRegistrationLeavesIdAndCreatedAtNull() {
        var customerId = UUID.randomUUID();
        var branchId = new BranchId(UUID.randomUUID());
        var domain = new CustomerRegistration(customerId, branchId);

        var entity = new CustomerRegistrationPersistenceEntity();
        CustomerRegistrationPersistenceAssembler.toEntity(domain, entity);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getVersion()).isNull();
        assertThat(entity.isNew()).isTrue();
        assertThat(entity.getCustomerId()).isEqualTo(customerId);
        assertThat(entity.getBranchId()).isEqualTo(branchId.value());
        assertThat(entity.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("existing domain customer registration with non-null version sets id and auditing fields (isNew = false)")
    void existingRegistrationSetsIdAndAuditingFields() {
        var registrationId = new CustomerId(UUID.randomUUID());
        var customerId = UUID.randomUUID();
        var branchId = new BranchId(UUID.randomUUID());
        var now = Instant.now();
        var domain = new CustomerRegistration(registrationId, customerId, branchId, CustomerRegistrationStatus.ACTIVE, now, now, null, 1L);

        var entity = new CustomerRegistrationPersistenceEntity();
        CustomerRegistrationPersistenceAssembler.toEntity(domain, entity);

        assertThat(entity.getId()).isEqualTo(registrationId.value());
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
        assertThat(entity.getVersion()).isEqualTo(1L);
        assertThat(entity.isNew()).isFalse();
    }
}
