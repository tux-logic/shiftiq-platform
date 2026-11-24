package com.tuxlogic.shiftiq.platform.core.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.ProfileCommandService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.ProfileQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.UserId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.CustomerRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.EmployeeRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.OwnerRepository;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.UserProfileContextResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link ProfileCommandService} that persists profile image URLs across Core profiles.
 */
@Service
@Transactional
public class ProfileCommandServiceImpl implements ProfileCommandService {

    private final OwnerRepository ownerRepository;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final ProfileQueryService profileQueryService;

    public ProfileCommandServiceImpl(
            OwnerRepository ownerRepository,
            EmployeeRepository employeeRepository,
            CustomerRepository customerRepository,
            ProfileQueryService profileQueryService) {
        this.ownerRepository = ownerRepository;
        this.employeeRepository = employeeRepository;
        this.customerRepository = customerRepository;
        this.profileQueryService = profileQueryService;
    }

    @Override
    public UserProfileContextResource updateProfileImage(UserId userId, String profileImageUrl) {
        String normalizedUrl = profileImageUrl != null ? profileImageUrl.trim() : null;
        boolean updated = false;

        var ownerOpt = ownerRepository.findByUserId(userId);
        if (ownerOpt.isPresent()) {
            var owner = ownerOpt.get();
            owner.updateProfileImage(normalizedUrl);
            ownerRepository.save(owner);
            updated = true;
        }

        var employeeOpt = employeeRepository.findByUserId(userId);
        if (employeeOpt.isPresent()) {
            var employee = employeeOpt.get();
            employee.updateProfileImage(normalizedUrl);
            employeeRepository.save(employee);
            updated = true;
        }

        var customerOpt = customerRepository.findByUserId(userId);
        if (customerOpt.isPresent()) {
            var customer = customerOpt.get();
            customer.updateProfileImage(normalizedUrl);
            customerRepository.save(customer);
            updated = true;
        }

        if (!updated) {
            throw new IllegalArgumentException("core.error.profile.notFound");
        }

        return profileQueryService.getUserProfileContext(userId);
    }
}
