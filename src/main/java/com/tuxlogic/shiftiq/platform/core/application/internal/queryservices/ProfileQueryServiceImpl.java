package com.tuxlogic.shiftiq.platform.core.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.core.application.queryservices.ProfileQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetProfileRolesByUserIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.CustomerRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.EmployeeRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.OwnerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetProfileByDocumentNumberQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.responses.ProfileSummary;

import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import java.util.concurrent.CompletableFuture;

@Service
@Transactional(readOnly = true)
public class ProfileQueryServiceImpl implements ProfileQueryService {

    private static final Logger log = LoggerFactory.getLogger(ProfileQueryServiceImpl.class);

    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String ROLE_OWNER = "OWNER";
    public static final String ROLE_EMPLOYEE = "EMPLOYEE";

    private final CustomerRepository customerRepository;
    private final OwnerRepository ownerRepository;
    private final EmployeeRepository employeeRepository;

    public ProfileQueryServiceImpl(
            CustomerRepository customerRepository,
            OwnerRepository ownerRepository,
            EmployeeRepository employeeRepository) {
        this.customerRepository = customerRepository;
        this.ownerRepository = ownerRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public List<String> handle(GetProfileRolesByUserIdQuery query) {
        log.debug("Fetching profile roles for user ID: {}", query.userId());
        
        CompletableFuture<Boolean> isCustomer = CompletableFuture.supplyAsync(() -> customerRepository.existsByUserId(query.userId()));
        CompletableFuture<Boolean> isOwner = CompletableFuture.supplyAsync(() -> ownerRepository.existsByUserId(query.userId()));
        CompletableFuture<Boolean> isEmployee = CompletableFuture.supplyAsync(() -> employeeRepository.existsByUserId(query.userId()));

        CompletableFuture.allOf(isCustomer, isOwner, isEmployee).join();

        List<String> roles = new ArrayList<>();
        try {
            if (isCustomer.get()) roles.add(ROLE_CUSTOMER);
            if (isOwner.get()) roles.add(ROLE_OWNER);
            if (isEmployee.get()) roles.add(ROLE_EMPLOYEE);
        } catch (Exception e) {
            log.error("Error evaluating user roles concurrently: {}", e.getMessage());
        }

        return roles;
    }

    @Override
    public Optional<ProfileSummary> handle(GetProfileByDocumentNumberQuery query) {
        log.debug("Searching profile by document number: {}", query.documentNumber());

        CompletableFuture<Optional<ProfileSummary>> customerFuture = CompletableFuture.supplyAsync(() ->
                customerRepository.findByDocumentNumber(query.documentNumber())
                        .map(c -> {
                            String firstName = c.getName() != null ? c.getName().firstName() : c.getBusinessName();
                            String lastName = c.getName() != null ? c.getName().lastName() : "";
                            return new ProfileSummary(c.getId().value(), c.getUserId().value(), firstName, lastName, c.getDocument().getDocumentType().name(), c.getDocument().getDocumentNumber(), ROLE_CUSTOMER);
                        })
        );

        CompletableFuture<Optional<ProfileSummary>> employeeFuture = CompletableFuture.supplyAsync(() ->
                employeeRepository.findByDocumentNumber(query.documentNumber())
                        .map(e -> new ProfileSummary(e.getId().value(), e.getUserId().value(), e.getName().firstName(), e.getName().lastName(), e.getDocument().getDocumentType().name(), e.getDocument().getDocumentNumber(), ROLE_EMPLOYEE))
        );

        CompletableFuture<Optional<ProfileSummary>> ownerFuture = CompletableFuture.supplyAsync(() ->
                ownerRepository.findByDocumentNumber(query.documentNumber())
                        .map(o -> new ProfileSummary(o.getId().value(), o.getUserId().value(), o.getName().firstName(), o.getName().lastName(), o.getDocument().getDocumentType().name(), o.getDocument().getDocumentNumber(), ROLE_OWNER))
        );

        CompletableFuture.allOf(customerFuture, employeeFuture, ownerFuture).join();

        try {
            var cOpt = customerFuture.get();
            if (cOpt.isPresent()) return cOpt;
            var eOpt = employeeFuture.get();
            if (eOpt.isPresent()) return eOpt;
            var oOpt = ownerFuture.get();
            if (oOpt.isPresent()) return oOpt;
        } catch (Exception e) {
            log.error("Error searching profile by document number concurrently: {}", e.getMessage());
        }

        return Optional.empty();
    }
}
