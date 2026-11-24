package com.tuxlogic.shiftiq.platform.operations.infrastructure.outboundservices;

import com.tuxlogic.shiftiq.platform.operations.application.outboundservices.ExternalEmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * ACL adapter for checking employee/mechanic existence in core context via database queries.
 */
@Service
public class ExternalEmployeeServiceImpl implements ExternalEmployeeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalEmployeeServiceImpl.class);
    private final JdbcTemplate jdbcTemplate;

    public ExternalEmployeeServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean existsEmployee(UUID employeeId) {
        if (employeeId == null) {
            return false;
        }
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM employees WHERE id = ?", Integer.class, employeeId);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            LOGGER.warn("Failed to verify employee existence for id: {}", employeeId, e);
            return false;
        }
    }

    @Override
    public UUID resolveActiveRegistrationId(UUID candidateId, UUID branchId) {
        if (candidateId == null || branchId == null) {
            return candidateId;
        }
        try {
            // 1. Check if candidateId is already an active registration id in this branch
            String sqlById = "SELECT id FROM employee_registrations WHERE id = ? AND branch_id = ? AND status = 'ACTIVE' AND deleted_at IS NULL LIMIT 1";
            var listById = jdbcTemplate.query(sqlById, (rs, rowNum) -> (UUID) rs.getObject("id"), candidateId, branchId);
            if (!listById.isEmpty()) {
                return listById.get(0);
            }

            // 2. Check if candidateId is an employee_id (core profile id) with an active registration in this branch
            String sqlByEmployeeId = "SELECT id FROM employee_registrations WHERE employee_id = ? AND branch_id = ? AND status = 'ACTIVE' AND deleted_at IS NULL LIMIT 1";
            var listByEmp = jdbcTemplate.query(sqlByEmployeeId, (rs, rowNum) -> (UUID) rs.getObject("id"), candidateId, branchId);
            if (!listByEmp.isEmpty()) {
                return listByEmp.get(0);
            }
        } catch (DataAccessException e) {
            LOGGER.warn("Failed to resolve active employee registration for candidateId: {}, branchId: {}", candidateId, branchId, e);
        }
        // Fallback to original candidateId (let standard validations or constraints handle it)
        return candidateId;
    }

    @Override
    public Optional<String> fetchMechanicName(UUID mechanicId) {
        if (mechanicId == null) {
            return Optional.empty();
        }
        try {
            String byRegistrationSql = """
                    SELECT TRIM(CONCAT(COALESCE(e.first_name, ''), ' ', COALESCE(e.last_name, '')))
                    FROM employee_registrations er
                    JOIN employees e ON er.employee_id = e.id
                    WHERE er.id = ? AND e.deleted_at IS NULL
                    LIMIT 1
                    """;
            var regNames = jdbcTemplate.query(byRegistrationSql, (rs, rowNum) -> rs.getString(1), mechanicId);
            if (!regNames.isEmpty() && regNames.get(0) != null && !regNames.get(0).isBlank()) {
                return Optional.of(regNames.get(0));
            }

            String byEmployeeSql = """
                    SELECT TRIM(CONCAT(COALESCE(first_name, ''), ' ', COALESCE(last_name, '')))
                    FROM employees
                    WHERE id = ? AND deleted_at IS NULL
                    LIMIT 1
                    """;
            var empNames = jdbcTemplate.query(byEmployeeSql, (rs, rowNum) -> rs.getString(1), mechanicId);
            if (!empNames.isEmpty() && empNames.get(0) != null && !empNames.get(0).isBlank()) {
                return Optional.of(empNames.get(0));
            }
        } catch (DataAccessException e) {
            LOGGER.warn("Failed to fetch mechanic name for mechanicId: {}", mechanicId, e);
        }
        return Optional.empty();
    }
}
