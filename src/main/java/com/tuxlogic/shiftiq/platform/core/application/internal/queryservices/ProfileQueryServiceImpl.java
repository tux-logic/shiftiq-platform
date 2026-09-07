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

import java.util.List;
import java.util.Optional;

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
        return customerRepository.findProfileRolesByUserId(query.userId());
    }

    @Override
    public Optional<ProfileSummary> handle(GetProfileByDocumentNumberQuery query) {
        log.debug("Searching profile by document number: {}", query.documentNumber());
        var customer = customerRepository.findByDocumentNumber(query.documentNumber());
        if (customer.isPresent()) {
            var c = customer.get();
            String firstName = c.getName() != null ? c.getName().firstName() : c.getBusinessName();
            String lastName = c.getName() != null ? c.getName().lastName() : "";
            return Optional.of(new ProfileSummary(c.getId().value(), c.getUserId().value(), firstName, lastName, c.getDocument().getDocumentType().name(), c.getDocument().getDocumentNumber(), ROLE_CUSTOMER));
        }

        var employee = employeeRepository.findByDocumentNumber(query.documentNumber());
        if (employee.isPresent()) {
            var e = employee.get();
            return Optional.of(new ProfileSummary(e.getId().value(), e.getUserId().value(), e.getName().firstName(), e.getName().lastName(), e.getDocument().getDocumentType().name(), e.getDocument().getDocumentNumber(), ROLE_EMPLOYEE));
        }

        var owner = ownerRepository.findByDocumentNumber(query.documentNumber());
        if (owner.isPresent()) {
            var o = owner.get();
            return Optional.of(new ProfileSummary(o.getId().value(), o.getUserId().value(), o.getName().firstName(), o.getName().lastName(), o.getDocument().getDocumentType().name(), o.getDocument().getDocumentNumber(), ROLE_OWNER));
        }

        return Optional.empty();
    }
}
