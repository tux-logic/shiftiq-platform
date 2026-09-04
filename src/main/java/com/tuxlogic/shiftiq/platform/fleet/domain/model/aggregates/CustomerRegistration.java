package com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.CustomerRegistrationId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.CustomerRegistrationStatus;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class CustomerRegistration extends AbstractDomainAggregateRoot<CustomerRegistration> {

    private CustomerRegistrationId id;
    private UUID customerId;
    private BranchId branchId;
    private CustomerRegistrationStatus status;
    private Instant createdAt;
    private Instant deletedAt;

    public CustomerRegistration() {
    }

    public CustomerRegistration(UUID customerId, BranchId branchId) {
        this.id = new CustomerRegistrationId(UUID.randomUUID());
        this.customerId = customerId;
        this.branchId = branchId;
        this.status = CustomerRegistrationStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public CustomerRegistration(CustomerRegistrationId id, UUID customerId, BranchId branchId, CustomerRegistrationStatus status, Instant createdAt, Instant deletedAt) {
        this.id = id;
        this.customerId = customerId;
        this.branchId = branchId;
        this.status = status;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public void deactivate() {
        this.status = CustomerRegistrationStatus.INACTIVE;
        this.deletedAt = Instant.now();
    }
}
