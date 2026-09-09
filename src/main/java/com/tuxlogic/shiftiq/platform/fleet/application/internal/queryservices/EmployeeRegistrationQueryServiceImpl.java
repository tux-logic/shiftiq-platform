package com.tuxlogic.shiftiq.platform.fleet.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.fleet.application.queryservices.EmployeeRegistrationQueryService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.EmployeeRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationByIdQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationByEmployeeIdQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationsByBranchIdQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationsByBranchIdAndStatusQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.EmployeeRegistrationQueryFailure;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.EmployeeRegistrationRepository;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EmployeeRegistrationQueryServiceImpl implements EmployeeRegistrationQueryService {

    private final EmployeeRegistrationRepository employeeRegistrationRepository;

    public EmployeeRegistrationQueryServiceImpl(EmployeeRegistrationRepository employeeRegistrationRepository) {
        this.employeeRegistrationRepository = employeeRegistrationRepository;
    }

    @Override
    public Result<EmployeeRegistration, EmployeeRegistrationQueryFailure> handle(GetEmployeeRegistrationByIdQuery query) {
        try {
            if (query == null || query.id() == null) {
                return Result.failure(EmployeeRegistrationQueryFailure.INVALID_QUERY_DATA);
            }
            var registrationOpt = employeeRegistrationRepository.findById(query.id());
            return registrationOpt.map(Result::<EmployeeRegistration, EmployeeRegistrationQueryFailure>success)
                    .orElseGet(() -> Result.failure(EmployeeRegistrationQueryFailure.EMPLOYEE_REGISTRATION_NOT_FOUND));
        } catch (IllegalArgumentException ex) {
            log.error("Failed to query employee registration by id: {}", ex.getMessage());
            return Result.failure(EmployeeRegistrationQueryFailure.INVALID_QUERY_DATA);
        } catch (Exception ex) {
            log.error("Unexpected error querying employee registration by id: {}", ex.getMessage(), ex);
            return Result.failure(EmployeeRegistrationQueryFailure.UNEXPECTED_ERROR);
        }
    }

    @Override
    public Result<EmployeeRegistration, EmployeeRegistrationQueryFailure> handle(GetEmployeeRegistrationByEmployeeIdQuery query) {
        try {
            if (query == null || query.employeeId() == null) {
                return Result.failure(EmployeeRegistrationQueryFailure.INVALID_QUERY_DATA);
            }
            var registrationOpt = employeeRegistrationRepository.findByEmployeeId(query.employeeId());
            return registrationOpt.map(Result::<EmployeeRegistration, EmployeeRegistrationQueryFailure>success)
                    .orElseGet(() -> Result.failure(EmployeeRegistrationQueryFailure.EMPLOYEE_REGISTRATION_NOT_FOUND));
        } catch (IllegalArgumentException ex) {
            log.error("Failed to query employee registration by employee id: {}", ex.getMessage());
            return Result.failure(EmployeeRegistrationQueryFailure.INVALID_QUERY_DATA);
        } catch (Exception ex) {
            log.error("Unexpected error querying employee registration by employee id: {}", ex.getMessage(), ex);
            return Result.failure(EmployeeRegistrationQueryFailure.UNEXPECTED_ERROR);
        }
    }

    @Override
    public Result<List<EmployeeRegistration>, EmployeeRegistrationQueryFailure> handle(GetEmployeeRegistrationsByBranchIdQuery query) {
        try {
            if (query == null || query.branchId() == null) {
                return Result.failure(EmployeeRegistrationQueryFailure.INVALID_QUERY_DATA);
            }
            var list = employeeRegistrationRepository.findByBranchId(query.branchId());
            return Result.success(list);
        } catch (IllegalArgumentException ex) {
            log.error("Failed to query employee registrations by branch id: {}", ex.getMessage());
            return Result.failure(EmployeeRegistrationQueryFailure.INVALID_QUERY_DATA);
        } catch (Exception ex) {
            log.error("Unexpected error querying employee registrations by branch id: {}", ex.getMessage(), ex);
            return Result.failure(EmployeeRegistrationQueryFailure.UNEXPECTED_ERROR);
        }
    }

    @Override
    public Result<List<EmployeeRegistration>, EmployeeRegistrationQueryFailure> handle(GetEmployeeRegistrationsByBranchIdAndStatusQuery query) {
        try {
            if (query == null || query.branchId() == null) {
                return Result.failure(EmployeeRegistrationQueryFailure.INVALID_QUERY_DATA);
            }
            var list = employeeRegistrationRepository.findByBranchIdAndStatus(query.branchId(), query.status());
            return Result.success(list);
        } catch (IllegalArgumentException ex) {
            log.error("Failed to query employee registrations by branch id and status: {}", ex.getMessage());
            return Result.failure(EmployeeRegistrationQueryFailure.INVALID_QUERY_DATA);
        } catch (Exception ex) {
            log.error("Unexpected error querying employee registrations by branch id and status: {}", ex.getMessage(), ex);
            return Result.failure(EmployeeRegistrationQueryFailure.UNEXPECTED_ERROR);
        }
    }
}
