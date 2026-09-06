package com.tuxlogic.shiftiq.platform.operations.infrastructure.outboundservices;

import com.tuxlogic.shiftiq.platform.operations.application.outboundservices.ExternalBranchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter encapsulating branch code query execution.
 */
@Service
public class ExternalBranchServiceImpl implements ExternalBranchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalBranchServiceImpl.class);
    private final JdbcTemplate jdbcTemplate;

    public ExternalBranchServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<String> fetchBranchCode(UUID branchId) {
        if (branchId == null) {
            return Optional.empty();
        }
        try {
            String code = jdbcTemplate.queryForObject("SELECT code FROM branches WHERE id = ?", String.class, branchId);
            return Optional.ofNullable(code);
        } catch (DataAccessException e) {
            LOGGER.warn("Failed to fetch branch code for branchId: {}", branchId, e);
            return Optional.empty();
        }
    }
}
