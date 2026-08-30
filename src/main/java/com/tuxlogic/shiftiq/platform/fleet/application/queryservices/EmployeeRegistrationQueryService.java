package com.tuxlogic.shiftiq.platform.fleet.application.queryservices;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.EmployeeRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationByIdQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationByEmployeeIdQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationsByBranchIdQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationsByBranchIdAndStatusQuery;

import java.util.List;
import java.util.Optional;

public interface EmployeeRegistrationQueryService {
    Optional<EmployeeRegistration> handle(GetEmployeeRegistrationByIdQuery query);
    Optional<EmployeeRegistration> handle(GetEmployeeRegistrationByEmployeeIdQuery query);
    List<EmployeeRegistration> handle(GetEmployeeRegistrationsByBranchIdQuery query);
    List<EmployeeRegistration> handle(GetEmployeeRegistrationsByBranchIdAndStatusQuery query);
}
