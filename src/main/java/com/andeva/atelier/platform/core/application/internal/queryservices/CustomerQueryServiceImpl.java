package com.andeva.atelier.platform.core.application.internal.queryservices;

import com.andeva.atelier.platform.core.application.queryservices.CustomerQueryService;
import com.andeva.atelier.platform.core.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.core.domain.model.queries.GetCustomerByIdQuery;
import com.andeva.atelier.platform.core.domain.model.queries.GetCustomerByUserIdQuery;
import com.andeva.atelier.platform.core.domain.repositories.CustomerRepository;
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
