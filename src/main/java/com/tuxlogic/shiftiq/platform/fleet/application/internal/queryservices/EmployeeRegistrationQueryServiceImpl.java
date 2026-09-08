package com.tuxlogic.shiftiq.platform.fleet.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.fleet.application.queryservices.EmployeeRegistrationQueryService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.EmployeeRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationByIdQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationByEmployeeIdQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationsByBranchIdQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationsByBranchIdAndStatusQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.EmployeeRegistrationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeRegistrationQueryServiceImpl implements EmployeeRegistrationQueryService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeRegistrationQueryServiceImpl.class);

    private final EmployeeRegistrationRepository employeeRegistrationRepository;

    public EmployeeRegistrationQueryServiceImpl(EmployeeRegistrationRepository employeeRegistrationRepository) {
        this.employeeRegistrationRepository = employeeRegistrationRepository;
    }

    @Override
    public Optional<EmployeeRegistration> handle(GetEmployeeRegistrationByIdQuery query) {
        try {
            return employeeRegistrationRepository.findById(query.id());
        } catch (IllegalArgumentException ex) {
            log.error("Failed to query employee registration by id: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<EmployeeRegistration> handle(GetEmployeeRegistrationByEmployeeIdQuery query) {
        try {
            return employeeRegistrationRepository.findByEmployeeId(query.employeeId());
        } catch (IllegalArgumentException ex) {
            log.error("Failed to query employee registration by employee id: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<EmployeeRegistration> handle(GetEmployeeRegistrationsByBranchIdQuery query) {
        try {
            return employeeRegistrationRepository.findByBranchId(query.branchId());
        } catch (IllegalArgumentException ex) {
            log.error("Failed to query employee registrations by branch id: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<EmployeeRegistration> handle(GetEmployeeRegistrationsByBranchIdAndStatusQuery query) {
        try {
            return employeeRegistrationRepository.findByBranchIdAndStatus(query.branchId(), query.status());
        } catch (IllegalArgumentException ex) {
            log.error("Failed to query employee registrations by branch id and status: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }
}
