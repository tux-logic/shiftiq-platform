package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Employee;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.UserId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.EmployeeRepository;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.assemblers.EmployeePersistenceAssembler;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.EmployeePersistenceEntity;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories.EmployeePersistenceRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class EmployeeRepositoryImpl implements EmployeeRepository {

    private final EmployeePersistenceRepository employeePersistenceRepository;

    public EmployeeRepositoryImpl(EmployeePersistenceRepository employeePersistenceRepository) {
        this.employeePersistenceRepository = employeePersistenceRepository;
    }

    @Override
    @Transactional
    public Employee save(Employee employee) {
        var entity = (employee.getId() != null)
                ? employeePersistenceRepository.findById(employee.getId().value()).orElseGet(EmployeePersistenceEntity::new)
                : new EmployeePersistenceEntity();
        
        EmployeePersistenceAssembler.toEntity(employee, entity);
        EmployeePersistenceEntity savedEntity = employeePersistenceRepository.save(entity);
        return EmployeePersistenceAssembler.toDomain(savedEntity);
    }

    @Override
    public Optional<Employee> findById(EmployeeId id) {
        return employeePersistenceRepository.findById(id.value()).map(EmployeePersistenceAssembler::toDomain);
    }

    @Override
    public Optional<Employee> findByUserId(UserId userId) {
        return employeePersistenceRepository.findByUserId(userId.value()).map(EmployeePersistenceAssembler::toDomain);
    }

    @Override
    public boolean existsByUserId(UserId userId) {
        return employeePersistenceRepository.existsByUserId(userId.value());
    }

    @Override
    public Optional<Employee> findByDocumentNumber(String documentNumber) {
        return employeePersistenceRepository.findByDocumentNumber(documentNumber).map(EmployeePersistenceAssembler::toDomain);
    }

    @Override
    @Transactional
    public void delete(Employee employee) {
        if (employee == null || employee.getId() == null) {
            throw new IllegalArgumentException("Employee or Employee ID cannot be null for deletion");
        }
        if (!employeePersistenceRepository.existsById(employee.getId().value())) {
            throw new IllegalArgumentException("Employee not found with ID: " + employee.getId().value());
        }
        employeePersistenceRepository.deleteById(employee.getId().value());
    }
}
