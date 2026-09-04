package com.tuxlogic.shiftiq.platform.fleet.application.queryservices;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.EmployeeRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationByIdQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationByEmployeeIdQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationsByBranchIdQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationsByBranchIdAndStatusQuery;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import org.springframework.data.domain.Page;

public interface EmployeeRegistrationQueryService {
    Result<EmployeeRegistration, EmployeeRegistrationQueryFailure> handle(GetEmployeeRegistrationByIdQuery query);
    Result<EmployeeRegistration, EmployeeRegistrationQueryFailure> handle(GetEmployeeRegistrationByEmployeeIdQuery query);
    Result<Page<EmployeeRegistration>, EmployeeRegistrationQueryFailure> handle(GetEmployeeRegistrationsByBranchIdQuery query);
    Result<Page<EmployeeRegistration>, EmployeeRegistrationQueryFailure> handle(GetEmployeeRegistrationsByBranchIdAndStatusQuery query);
}
