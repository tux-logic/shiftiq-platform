package com.andeva.atelier.platform.core.application.internal.queryservices;

import com.andeva.atelier.platform.core.application.queryservices.EmployeeQueryService;
import com.andeva.atelier.platform.core.domain.model.aggregates.Employee;
import com.andeva.atelier.platform.core.domain.model.queries.GetEmployeeByIdQuery;
import com.andeva.atelier.platform.core.domain.model.queries.GetEmployeeByUserIdQuery;
import com.andeva.atelier.platform.core.domain.model.queries.GetEmployeeByDocumentNumberQuery;
import com.andeva.atelier.platform.core.domain.repositories.EmployeeRepository;
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
