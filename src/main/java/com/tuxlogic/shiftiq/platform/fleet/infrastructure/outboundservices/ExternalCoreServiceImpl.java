package com.tuxlogic.shiftiq.platform.fleet.infrastructure.outboundservices;

import com.tuxlogic.shiftiq.platform.core.application.queryservices.BranchQueryService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.CustomerQueryService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.EmployeeQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetBranchByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetCustomerByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetEmployeeByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;
import com.tuxlogic.shiftiq.platform.fleet.application.outboundservices.ExternalCoreService;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import org.springframework.stereotype.Service;

/**
 * ACL adapter delegating existence checks to the core context through its
 * application layer, so fleet never couples to core repositories.
 */
@Service
public class ExternalCoreServiceImpl implements ExternalCoreService {

    private final BranchQueryService branchQueryService;
    private final CustomerQueryService customerQueryService;
    private final EmployeeQueryService employeeQueryService;

    public ExternalCoreServiceImpl(BranchQueryService branchQueryService,
                                   CustomerQueryService customerQueryService,
                                   EmployeeQueryService employeeQueryService) {
        this.branchQueryService = branchQueryService;
        this.customerQueryService = customerQueryService;
        this.employeeQueryService = employeeQueryService;
    }

    @Override
    public boolean existsBranchById(BranchId branchId) {
        if (branchId == null) return false;
        return branchQueryService.handle(new GetBranchByIdQuery(branchId)).isPresent();
    }

    @Override
    public boolean existsCustomerById(CustomerId customerId) {
        if (customerId == null) return false;
        return customerQueryService.handle(new GetCustomerByIdQuery(customerId)).isPresent();
    }

    @Override
    public boolean existsEmployeeById(EmployeeId employeeId) {
        if (employeeId == null) return false;
        return employeeQueryService.handle(new GetEmployeeByIdQuery(employeeId)).isPresent();
    }
}
