package com.tuxlogic.shiftiq.platform.core.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.core.application.queryservices.EmployeeQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Employee;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetEmployeeByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetEmployeeByUserIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetEmployeeByDocumentNumberQuery;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmployeeQueryServiceImpl implements EmployeeQueryService {
    private final EmployeeRepository employeeRepository;

    public EmployeeQueryServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Optional<Employee> handle(GetEmployeeByIdQuery query) {
        return employeeRepository.findById(query.id());
    }

    @Override
    public Optional<Employee> handle(GetEmployeeByUserIdQuery query) {
        return employeeRepository.findByUserId(query.userId());
    }

    @Override
    public Optional<Employee> handle(GetEmployeeByDocumentNumberQuery query) {
        return employeeRepository.findByDocumentNumber(query.documentNumber());
    }
}
