package com.tuxlogic.shiftiq.platform.billing.infrastructure.outbound.acl;

import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.ExternalBillingContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * JDBC-based ACL adapter querying work order ownership and completion status across bounded contexts.
 */
@Service
public class ExternalBillingContextServiceImpl implements ExternalBillingContextService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalBillingContextServiceImpl.class);
    private final JdbcTemplate jdbcTemplate;

    public ExternalBillingContextServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean isCustomerOwnerOfWorkOrder(UUID userId, UUID workOrderId) {
        if (userId == null || workOrderId == null) {
            return false;
        }
        try {
            Integer count = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(1)
                    FROM work_orders w
                    JOIN customers c ON w.customer_id = c.id
                    WHERE w.id = ?
                      AND c.user_id = ?
                      AND w.deleted_at IS NULL
                      AND c.deleted_at IS NULL
                    """,
                    Integer.class,
                    workOrderId,
                    userId
            );
            return count != null && count > 0;
        } catch (DataAccessException e) {
            LOGGER.warn("Failed to verify customer ownership for workOrderId={} userId={}", workOrderId, userId, e);
            return false;
        }
    }

    @Override
    public boolean isWorkOrderCompleted(UUID workOrderId) {
        if (workOrderId == null) {
            return false;
        }
        try {
            String status = jdbcTemplate.queryForObject(
                    "SELECT status FROM work_orders WHERE id = ? AND deleted_at IS NULL",
                    String.class,
                    workOrderId
            );
            return "COMPLETED".equalsIgnoreCase(status);
        } catch (DataAccessException e) {
            LOGGER.warn("Failed to verify completion status for workOrderId={}", workOrderId, e);
            return false;
        }
    }

    @Override
    public List<UUID> findWorkOrderIdsByUserId(UUID userId) {
        if (userId == null) {
            return List.of();
        }
        try {
            return jdbcTemplate.queryForList(
                    """
                    SELECT w.id
                    FROM work_orders w
                    JOIN customers c ON w.customer_id = c.id
                    WHERE c.user_id = ?
                      AND w.deleted_at IS NULL
                      AND c.deleted_at IS NULL
                    """,
                    UUID.class,
                    userId
            );
        } catch (DataAccessException e) {
            LOGGER.warn("Failed to list workOrderIds for userId={}", userId, e);
            return List.of();
        }
    }
}
