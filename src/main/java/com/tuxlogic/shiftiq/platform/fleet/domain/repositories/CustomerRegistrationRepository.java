package com.tuxlogic.shiftiq.platform.fleet.domain.repositories;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.CustomerRegistration;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRegistrationRepository {
    CustomerRegistration save(CustomerRegistration registration);
    Optional<CustomerRegistration> findById(UUID id);
    Optional<CustomerRegistration> findByCustomerId(UUID customerId);
    Optional<CustomerRegistration> findByCustomerIdAndBranchId(UUID customerId, UUID branchId);
    Page<CustomerRegistration> findByBranchIdAndStatus(BranchId branchId, String status, Pageable pageable);
    boolean existsByCustomerIdAndBranchId(UUID customerId, UUID branchId);
}




