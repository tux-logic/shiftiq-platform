package com.tuxlogic.shiftiq.platform.fleet.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.fleet.application.queryservices.EmployeeRegistrationQueryFailure;
import com.tuxlogic.shiftiq.platform.fleet.application.queryservices.EmployeeRegistrationQueryService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.EmployeeRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationByIdQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationByEmployeeIdQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationsByBranchIdQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationsByBranchIdAndStatusQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.EmployeeRegistrationRepository;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class EmployeeRegistrationQueryServiceImpl implements EmployeeRegistrationQueryService {

    private final EmployeeRegistrationRepository employeeRegistrationRepository;

    public EmployeeRegistrationQueryServiceImpl(EmployeeRegistrationRepository employeeRegistrationRepository) {
        this.employeeRegistrationRepository = employeeRegistrationRepository;
    }

    @Override
    public Result<EmployeeRegistration, EmployeeRegistrationQueryFailure> handle(GetEmployeeRegistrationByIdQuery query) {
        return employeeRegistrationRepository.findById(query.id())
                .map(Result::<EmployeeRegistration, EmployeeRegistrationQueryFailure>success)
                .orElseGet(() -> Result.failure(new EmployeeRegistrationQueryFailure.NotFound(
                        "Employee registration not found for ID: " + query.id())));
    }

    @Override
    public Result<EmployeeRegistration, EmployeeRegistrationQueryFailure> handle(GetEmployeeRegistrationByEmployeeIdQuery query) {
        return employeeRegistrationRepository.findByEmployeeId(query.employeeId())
                .map(Result::<EmployeeRegistration, EmployeeRegistrationQueryFailure>success)
                .orElseGet(() -> Result.failure(new EmployeeRegistrationQueryFailure.NotFound(
                        "Employee registration not found for Employee ID: " + query.employeeId())));
    }

    @Override
    public Result<Page<EmployeeRegistration>, EmployeeRegistrationQueryFailure> handle(GetEmployeeRegistrationsByBranchIdQuery query) {
        var registrations = employeeRegistrationRepository.findByBranchId(query.branchId(), query.pageable());
        return Result.success(registrations);
    }

    @Override
    public Result<Page<EmployeeRegistration>, EmployeeRegistrationQueryFailure> handle(GetEmployeeRegistrationsByBranchIdAndStatusQuery query) {
        var registrations = employeeRegistrationRepository.findByBranchIdAndStatus(query.branchId(), query.status(), query.pageable());
        return Result.success(registrations);
    }
}
