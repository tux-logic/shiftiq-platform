package com.tuxlogic.shiftiq.platform.core.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.core.application.queryservices.CustomerQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Customer;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetCustomerByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetCustomerByUserIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerQueryServiceImpl implements CustomerQueryService {
    private final CustomerRepository customerRepository;

    public CustomerQueryServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Optional<Customer> handle(GetCustomerByIdQuery query) {
        return customerRepository.findById(query.id());
    }

    @Override
    public Optional<Customer> handle(GetCustomerByUserIdQuery query) {
        return customerRepository.findByUserId(query.userId());
    }
}
