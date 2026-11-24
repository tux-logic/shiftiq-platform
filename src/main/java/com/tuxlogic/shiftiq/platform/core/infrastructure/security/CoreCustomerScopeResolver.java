package com.tuxlogic.shiftiq.platform.core.infrastructure.security;

import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories.CustomerPersistenceRepository;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.CustomerScopeResolver;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Core implementation of {@link CustomerScopeResolver}: user -&gt; customer profile.
 */
@Component
public class CoreCustomerScopeResolver implements CustomerScopeResolver {

    private final CustomerPersistenceRepository customerRepository;

    public CoreCustomerScopeResolver(CustomerPersistenceRepository customerRepository) {
        this.customerRepository = customerRepository;
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
}
