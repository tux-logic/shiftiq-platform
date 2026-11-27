package com.tuxlogic.shiftiq.platform.core.infrastructure.security;

import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories.CustomerPersistenceRepository;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.CustomerScopeResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Core implementation of {@link CustomerScopeResolver}: user -&gt; customer profile and workshop scope.
 */
@Component
public class CoreCustomerScopeResolver implements CustomerScopeResolver {

    private final CustomerPersistenceRepository customerRepository;
    private final JdbcTemplate jdbcTemplate;

    public CoreCustomerScopeResolver(CustomerPersistenceRepository customerRepository, JdbcTemplate jdbcTemplate) {
        this.customerRepository = customerRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UUID findCustomerIdForUser(UUID userId) {
        if (userId == null) {
            return null;
        }
        return customerRepository.findByUserId(userId)
                .map(customer -> customer.getId())
                .orElse(null);
    }

    @Override
    public List<UUID> findCustomerWorkshopIds(UUID customerId) {
        if (customerId == null) {
            return List.of();
        }
        return jdbcTemplate.queryForList("""
                SELECT DISTINCT b.workshop_id
                FROM customer_registrations cr
                JOIN branches b ON b.id = cr.branch_id
                WHERE cr.customer_id = ?
                  AND cr.status = 'ACTIVE'
                  AND cr.deleted_at IS NULL
                  AND b.deleted_at IS NULL
                """, UUID.class, customerId);
    }

    @Override
    public boolean isCustomerAssociatedWithBranchWorkshop(UUID customerId, UUID branchId) {
        if (customerId == null || branchId == null) {
            return false;
        }
        Boolean associated = jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1
                    FROM customer_registrations cr
                    JOIN branches b_reg ON b_reg.id = cr.branch_id
                    JOIN branches b_target ON b_target.id = ?
                    WHERE cr.customer_id = ?
                      AND cr.status = 'ACTIVE'
                      AND cr.deleted_at IS NULL
                      AND b_reg.deleted_at IS NULL
                      AND b_target.deleted_at IS NULL
                      AND b_reg.workshop_id = b_target.workshop_id
                )
                """, Boolean.class, branchId, customerId);
        return Boolean.TRUE.equals(associated);
    }
}
