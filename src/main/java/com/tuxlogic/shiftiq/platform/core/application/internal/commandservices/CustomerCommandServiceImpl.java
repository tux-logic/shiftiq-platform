package com.tuxlogic.shiftiq.platform.core.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.CustomerCommandService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Customer;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateCustomerCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.DeleteCustomerCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateCustomerCommand;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Document;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.DocumentType;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.CustomerRepository;
import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.identity.IdentityValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CustomerCommandServiceImpl implements CustomerCommandService {

    private static final Logger log = LoggerFactory.getLogger(CustomerCommandServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final IdentityValidationService identityValidationService;

    public CustomerCommandServiceImpl(CustomerRepository customerRepository,
                                      IdentityValidationService identityValidationService) {
        this.customerRepository = customerRepository;
        this.identityValidationService = identityValidationService;
    }

    @Override
    public Optional<Customer> handle(CreateCustomerCommand command) {
        if (customerRepository.existsByUserId(command.userId())) {
            throw new IllegalArgumentException("core.error.customer.profileAlreadyExists");
        }

        validateDocument(command.document(), command.isCorporate());

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

        validateDocument(command.document(), customer.isCorporate());

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

    private void validateDocument(Document document, boolean isCorporate) {
        if (document == null) {
            return;
        }
        var docType = document.getDocumentType();
        String docNumber = document.getDocumentNumber();

        if (isCorporate) {
            if (docType != DocumentType.RUC) {
                throw new IllegalArgumentException("billing.error.invoice.ruc.documentType");
            }
            var companyOpt = identityValidationService.validateRuc(docNumber);
            if (companyOpt.isEmpty()) {
                throw new IllegalArgumentException("shared.error.identity.notFound");
            }
            var company = companyOpt.get();
            if (!company.active()) {
                throw new IllegalArgumentException("shared.error.identity.rucInactive");
            }
            if (!company.habido()) {
                throw new IllegalArgumentException("shared.error.identity.rucNotHabido");
            }
        } else {
            if (docType == DocumentType.DNI) {
                var personOpt = identityValidationService.validateDni(docNumber);
                if (personOpt.isEmpty()) {
                    throw new IllegalArgumentException("shared.error.identity.notFound");
                }
            } else if (docType == DocumentType.CE) {
                var personOpt = identityValidationService.validateCe(docNumber);
                if (personOpt.isEmpty()) {
                    throw new IllegalArgumentException("shared.error.identity.notFound");
                }
            }
        }
    }

    @Override
    public void handle(DeleteCustomerCommand command) {
        var existingCustomer = customerRepository.findById(command.customerId())
                .orElseThrow(() -> new IllegalArgumentException("core.error.customer.notFound"));
        
        customerRepository.delete(existingCustomer);
        log.info("Deleted Customer profile ID '{}'", command.customerId().value());
    }
}
