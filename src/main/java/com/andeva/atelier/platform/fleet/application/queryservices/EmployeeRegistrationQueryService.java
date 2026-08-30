package com.andeva.atelier.platform.fleet.application.queryservices;

import com.andeva.atelier.platform.fleet.domain.model.aggregates.EmployeeRegistration;
import com.andeva.atelier.platform.fleet.domain.model.queries.GetEmployeeRegistrationByIdQuery;
import com.andeva.atelier.platform.fleet.domain.model.queries.GetEmployeeRegistrationByEmployeeIdQuery;
import com.andeva.atelier.platform.fleet.domain.model.queries.GetEmployeeRegistrationsByBranchIdQuery;
import com.andeva.atelier.platform.fleet.domain.model.queries.GetEmployeeRegistrationsByBranchIdAndStatusQuery;

import java.util.List;
import java.util.Optional;

public interface EmployeeRegistrationQueryService {
    Optional<EmployeeRegistration> handle(GetEmployeeRegistrationByIdQuery query);
    Optional<EmployeeRegistration> handle(GetEmployeeRegistrationByEmployeeIdQuery query);
    List<EmployeeRegistration> handle(GetEmployeeRegistrationsByBranchIdQuery query);
    List<EmployeeRegistration> handle(GetEmployeeRegistrationsByBranchIdAndStatusQuery query);
}
