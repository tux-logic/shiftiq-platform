package com.andeva.atelier.platform.fleet.domain.model.aggregates;

import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.fleet.domain.model.valueobjects.CustomerRegistrationStatus;
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
    private Instant deletedAt;

    public CustomerRegistration() {
    }

    public CustomerRegistration(UUID customerId, BranchId branchId) {
        this.id = new CustomerId(UUID.randomUUID());
        this.customerId = customerId;
        this.branchId = branchId;
        this.status = CustomerRegistrationStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public CustomerRegistration(CustomerId id, UUID customerId, BranchId branchId, CustomerRegistrationStatus status, Instant createdAt, Instant deletedAt) {
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

