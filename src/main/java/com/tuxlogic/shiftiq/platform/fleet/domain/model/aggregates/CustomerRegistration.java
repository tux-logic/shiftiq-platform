package com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates;

import com.tuxlogic.shiftiq.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.CustomerRegistrationStatus;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class CustomerRegistration extends AbstractDomainAggregateRoot<CustomerRegistration> {

    private CustomerId id;
    private UUID customerId;
    private BranchId branchId;
    private CustomerRegistrationStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private Long version;

    public CustomerRegistration() {
    }

    public CustomerRegistration(UUID customerId, BranchId branchId) {
        this.id = new CustomerId(UUID.randomUUID());
        this.customerId = customerId;
        this.branchId = branchId;
        this.status = CustomerRegistrationStatus.ACTIVE;
        this.registerEvent(new com.tuxlogic.shiftiq.platform.fleet.domain.model.events.CustomerRegistrationCreatedEvent(
                this, this.id.value(), new CustomerId(this.customerId), this.branchId
        ));
    }

    public CustomerRegistration(CustomerId id, UUID customerId, BranchId branchId, CustomerRegistrationStatus status, Instant createdAt, Instant updatedAt, Instant deletedAt, Long version) {
        this.id = id;
        this.customerId = customerId;
        this.branchId = branchId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.version = version;
    }

    public CustomerRegistration(CustomerId id, UUID customerId, BranchId branchId, CustomerRegistrationStatus status, Instant createdAt, Instant updatedAt, Instant deletedAt) {
        this(id, customerId, branchId, status, createdAt, updatedAt, deletedAt, null);
    }

    public void deactivate() {
        this.status = CustomerRegistrationStatus.INACTIVE;
        this.updatedAt = Instant.now();
        this.deletedAt = Instant.now();
    }
}


