package com.tuxlogic.shiftiq.platform.operations.infrastructure.outboundservices;

import com.tuxlogic.shiftiq.platform.operations.application.outboundservices.ExternalEmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

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
}
