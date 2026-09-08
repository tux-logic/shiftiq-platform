package com.tuxlogic.shiftiq.platform.fleet.infrastructure.outboundservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.BranchRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.CustomerRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.EmployeeRepository;
import com.tuxlogic.shiftiq.platform.fleet.application.outboundservices.ExternalCoreService;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import org.springframework.stereotype.Service;

@Service
public class ExternalCoreServiceImpl implements ExternalCoreService {

    private final BranchRepository branchRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;

    public ExternalCoreServiceImpl(BranchRepository branchRepository,
                                   CustomerRepository customerRepository,
                                   EmployeeRepository employeeRepository) {
        this.branchRepository = branchRepository;
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public boolean existsBranchById(BranchId branchId) {
        if (branchId == null) return false;
        return branchRepository.existsById(branchId);
    }

    @Override
    public boolean existsCustomerById(CustomerId customerId) {
        if (customerId == null) return false;
        return customerRepository.findById(customerId).isPresent();
    }

    @Override
    public boolean existsEmployeeById(EmployeeId employeeId) {
        if (employeeId == null) return false;
        return employeeRepository.findById(employeeId).isPresent();
    }
}
