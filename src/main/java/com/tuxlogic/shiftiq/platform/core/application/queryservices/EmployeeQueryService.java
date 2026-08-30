package com.tuxlogic.shiftiq.platform.core.application.queryservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Employee;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetEmployeeByIdQuery;

import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetEmployeeByUserIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetEmployeeByDocumentNumberQuery;

import java.util.Optional;

public interface EmployeeQueryService {
    Optional<Employee> handle(GetEmployeeByIdQuery query);
    Optional<Employee> handle(GetEmployeeByUserIdQuery query);
    Optional<Employee> handle(GetEmployeeByDocumentNumberQuery query);
}
