package com.tuxlogic.shiftiq.platform.fleet.application.queryservices;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.CustomerRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.CustomerRegistrationStatus;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetCustomerRegistrationByCustomerIdQuery;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface CustomerRegistrationQueryService {

    Result<Page<CustomerRegistration>, CustomerRegistrationQueryFailure> handle(BranchId branchId, Pageable pageable);

    Result<Page<CustomerRegistration>, CustomerRegistrationQueryFailure> handle(BranchId branchId, CustomerRegistrationStatus status, Pageable pageable);

    Result<CustomerRegistration, CustomerRegistrationQueryFailure> handle(UUID registrationId);

    Result<CustomerRegistration, CustomerRegistrationQueryFailure> handle(GetCustomerRegistrationByCustomerIdQuery query);
}


