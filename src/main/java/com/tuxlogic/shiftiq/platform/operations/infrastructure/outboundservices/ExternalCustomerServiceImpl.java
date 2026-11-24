package com.tuxlogic.shiftiq.platform.operations.infrastructure.outboundservices;

import com.tuxlogic.shiftiq.platform.operations.application.outboundservices.ExternalCustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter encapsulating customer display name query execution across the Operations-Core boundary.
 */
@Service("operationsExternalCustomerService")
public class ExternalCustomerServiceImpl implements ExternalCustomerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalCustomerServiceImpl.class);
    private final JdbcTemplate jdbcTemplate;

    public ExternalCustomerServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<String> fetchCustomerName(UUID customerId) {
        if (customerId == null) {
            return Optional.empty();
        }
        try {
            String name = jdbcTemplate.queryForObject(
                    """
                    SELECT CASE
                        WHEN is_corporate = true AND business_name IS NOT NULL AND TRIM(business_name) <> ''
                            THEN TRIM(business_name)
                        ELSE TRIM(CONCAT(COALESCE(first_name, ''), ' ', COALESCE(last_name, '')))
                    END
                    FROM customers
                    WHERE id = ? AND deleted_at IS NULL
                    """,
                    String.class,
                    customerId
            );
            if (name == null || name.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(name);
        } catch (DataAccessException e) {
            LOGGER.warn("Failed to fetch customer name for customerId: {}", customerId, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<UUID> fetchCustomerIdByUserId(UUID userId) {
        if (userId == null) {
            return Optional.empty();
        }
        try {
            UUID customerId = jdbcTemplate.queryForObject(
                    "SELECT id FROM customers WHERE user_id = ? AND deleted_at IS NULL",
                    UUID.class,
                    userId
            );
            return Optional.ofNullable(customerId);
        } catch (DataAccessException e) {
            LOGGER.debug("No customer profile found for userId: {}", userId);
            return Optional.empty();
        }
    }
}
