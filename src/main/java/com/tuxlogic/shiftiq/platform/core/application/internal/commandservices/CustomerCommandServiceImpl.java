package com.tuxlogic.shiftiq.platform.core.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.CustomerCommandService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Customer;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateCustomerCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.DeleteCustomerCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateCustomerCommand;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerCommandServiceImpl implements CustomerCommandService {

    private static final Logger log = LoggerFactory.getLogger(CustomerCommandServiceImpl.class);

    private final CustomerRepository customerRepository;

    public CustomerCommandServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Optional<Customer> handle(CreateCustomerCommand command) {
        if (customerRepository.existsByUserId(command.userId())) {
            throw new IllegalArgumentException("core.error.customer.profileAlreadyExists");
        }

        var customer = new Customer(
                command.userId(),
                command.isCorporate(),
                command.name(),
                command.businessName(),
                command.document(),
                command.phone()
        );

        var savedCustomer = customerRepository.save(customer);
        log.info("Created Customer profile ID '{}' for user ID '{}'", savedCustomer.getId().value(), command.userId().value());
        return Optional.of(savedCustomer);
    }

    @Override
    public Optional<Customer> handle(UpdateCustomerCommand command) {
        var customer = customerRepository.findById(command.customerId())
                .orElseThrow(() -> new IllegalArgumentException("core.error.customer.notFound"));
        
        customer.update(
            command.name(), 
            command.businessName(), 
            command.document(), 
            command.phone()
        );
        
        var savedCustomer = customerRepository.save(customer);
        log.info("Updated Customer profile ID '{}'", savedCustomer.getId().value());
        return Optional.of(savedCustomer);
    }

    @Override
    public void handle(DeleteCustomerCommand command) {
        var existingCustomer = customerRepository.findById(command.customerId())
                .orElseThrow(() -> new IllegalArgumentException("core.error.customer.notFound"));
        
        customerRepository.delete(existingCustomer);
        log.info("Deleted Customer profile ID '{}'", command.customerId().value());
    }
}
