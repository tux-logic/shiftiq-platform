package com.tuxlogic.shiftiq.platform.iot.infrastructure.acl;

import com.tuxlogic.shiftiq.platform.core.application.queryservices.CustomerQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetCustomerByIdQuery;
import com.tuxlogic.shiftiq.platform.fleet.application.queryservices.CustomerRegistrationQueryService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.CustomerRegistrationStatus;
import com.tuxlogic.shiftiq.platform.iot.domain.services.CustomerDirectoryPort;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing {@link CustomerDirectoryPort} against the published
 * application services of `core` and `fleet`. This is the only class in
 * `iot` allowed to know about those services directly.
 */
@Component
public class CustomerDirectoryPortImpl implements CustomerDirectoryPort {

    private final CustomerQueryService customerQueryService;
    private final CustomerRegistrationQueryService customerRegistrationQueryService;

    public CustomerDirectoryPortImpl(
            CustomerQueryService customerQueryService,
            CustomerRegistrationQueryService customerRegistrationQueryService
    ) {
        this.customerQueryService = customerQueryService;
        this.customerRegistrationQueryService = customerRegistrationQueryService;
    }

    @Override
    public Optional<UUID> findUserIdByCustomerId(UUID customerId) {
        return customerQueryService.handle(new GetCustomerByIdQuery(new CustomerId(customerId)))
                .map(customer -> customer.getUserId().value());
    }

    @Override
    public List<UUID> findUserIdsByCustomerIds(List<UUID> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) {
            return List.of();
        }
        return customerIds.stream()
                .map(this::findUserIdByCustomerId)
                .flatMap(Optional::stream)
                .distinct()
                .toList();
    }

    @Override
    public List<UUID> findActiveCustomerIdsByBranchId(BranchId branchId) {
        var result = customerRegistrationQueryService.handle(branchId, CustomerRegistrationStatus.ACTIVE);
        return result.success().orElseGet(List::of).stream()
                .map(registration -> registration.getCustomerId())
                .toList();
    }
}