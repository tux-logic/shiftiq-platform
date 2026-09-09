package com.tuxlogic.shiftiq.platform.fleet.domain.repositories;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.CustomerRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.CustomerRegistrationStatus;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRegistrationRepository {
    CustomerRegistration save(CustomerRegistration registration);
    Optional<CustomerRegistration> findById(UUID id);
    Optional<CustomerRegistration> findByCustomerId(UUID customerId);
    Optional<CustomerRegistration> findByCustomerIdAndBranchId(UUID customerId, UUID branchId);
    List<CustomerRegistration> findByBranchIdAndStatus(BranchId branchId, CustomerRegistrationStatus status);
    boolean existsByCustomerIdAndBranchId(UUID customerId, UUID branchId);
}


