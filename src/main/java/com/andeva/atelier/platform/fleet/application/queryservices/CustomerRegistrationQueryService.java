package com.andeva.atelier.platform.fleet.application.queryservices;

import com.andeva.atelier.platform.fleet.domain.model.aggregates.CustomerRegistration;
import com.andeva.atelier.platform.fleet.domain.model.valueobjects.CustomerRegistrationStatus;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.fleet.domain.model.queries.GetCustomerRegistrationByCustomerIdQuery;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;

import java.util.List;
import java.util.UUID;

public interface CustomerRegistrationQueryService {

    Result<List<CustomerRegistration>, CustomerRegistrationQueryFailure> handle(BranchId branchId);

    Result<List<CustomerRegistration>, CustomerRegistrationQueryFailure> handle(BranchId branchId, CustomerRegistrationStatus status);

    Result<CustomerRegistration, CustomerRegistrationQueryFailure> handle(UUID registrationId);

    Result<CustomerRegistration, CustomerRegistrationQueryFailure> handle(GetCustomerRegistrationByCustomerIdQuery query);
}

