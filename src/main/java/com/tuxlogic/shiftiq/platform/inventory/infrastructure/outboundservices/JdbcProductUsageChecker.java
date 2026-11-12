package com.tuxlogic.shiftiq.platform.inventory.infrastructure.outboundservices;

import com.tuxlogic.shiftiq.platform.inventory.application.outboundservices.ProductUsageChecker;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Checks Operations references without coupling Inventory to the Operations domain model.
 */
@Component
public class JdbcProductUsageChecker implements ProductUsageChecker {

    private static final String EXISTS_REFERENCE_SQL = """
            SELECT EXISTS (
                SELECT 1
                FROM work_order_task_products
                WHERE product_id = ?
                  AND deleted_at IS NULL
            )
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcProductUsageChecker(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean isReferencedByWorkOrderTasks(UUID productId) {
        if (productId == null) {
            return false;
        }
        Boolean exists = jdbcTemplate.queryForObject(EXISTS_REFERENCE_SQL, Boolean.class, productId);
        return Boolean.TRUE.equals(exists);
    }
}
