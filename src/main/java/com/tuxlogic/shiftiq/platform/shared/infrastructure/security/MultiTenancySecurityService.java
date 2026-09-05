package com.tuxlogic.shiftiq.platform.shared.infrastructure.security;

import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.AppointmentRepository;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.CustomerRegistrationRepository;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.EmployeeRegistrationRepository;
import com.tuxlogic.shiftiq.platform.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service to validate multi-tenancy access ensuring the requested branchId is validated
 * against the authenticated user session.
 *
 * <p><b>NOTE</b>: Full branch-membership validation is pending until IAM provides
 * {@code UserDetailsImpl#getBranchIds()}.
 * TODO: Implement branch validation after IAM update — replace the authentication-only check
 * in {@link #isAuthorizedForBranch} with a real membership lookup once the field is available.
 * </p>
 */
@Service("multiTenancySecurityService")
public class MultiTenancySecurityService {

    private final AppointmentRepository appointmentRepository;
    private final CustomerRegistrationRepository customerRegistrationRepository;
    private final EmployeeRegistrationRepository employeeRegistrationRepository;

    public MultiTenancySecurityService(
            AppointmentRepository appointmentRepository,
            CustomerRegistrationRepository customerRegistrationRepository,
            EmployeeRegistrationRepository employeeRegistrationRepository) {
        this.appointmentRepository = appointmentRepository;
        this.customerRegistrationRepository = customerRegistrationRepository;
        this.employeeRegistrationRepository = employeeRegistrationRepository;
    }

    /**
     * Verifies that the currently authenticated user has access to the requested branch.
     *
     * <p>TEMPORARY: Currently only checks that the request comes from an authenticated user.
     * TODO: Implement branch validation after IAM update — compare {@code branchId} against
     * {@code ((UserDetailsImpl) principal).getBranchIds()} once that field exists.
     * </p>
     */
    public boolean isAuthorizedForBranch(UUID branchId) {
        if (branchId == null) {
            return true;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetailsImpl)) {
            return false;
        }
        // TODO: Implement branch validation after IAM update
        // Uncomment and adapt once UserDetailsImpl exposes getBranchIds():
        //   return ((UserDetailsImpl) principal).getBranchIds().contains(branchId);
        return true;
    }

    /**
     * Verifies that the authenticated user is authorized to access the given appointment by ID.
     * Fetches the appointment to extract its branchId and delegates to {@link #isAuthorizedForBranch}.
     */
    public boolean isAuthorizedForAppointment(UUID appointmentId) {
        if (appointmentId == null) return false;
        return appointmentRepository.findById(appointmentId)
                .map(a -> isAuthorizedForBranch(a.getBranchId() != null ? a.getBranchId().value() : null))
                .orElse(true); // Entity not found → let the service handle 404
    }

    /**
     * Verifies that the authenticated user is authorized to access the given customer registration by ID.
     */
    public boolean isAuthorizedForCustomerRegistration(UUID registrationId) {
        if (registrationId == null) return false;
        return customerRegistrationRepository.findById(registrationId)
                .map(r -> isAuthorizedForBranch(r.getBranchId() != null ? r.getBranchId().value() : null))
                .orElse(true);
    }

    /**
     * Verifies that the authenticated user is authorized to access the given employee registration by ID.
     */
    public boolean isAuthorizedForEmployeeRegistration(UUID registrationId) {
        if (registrationId == null) return false;
        return employeeRegistrationRepository.findById(registrationId)
                .map(r -> isAuthorizedForBranch(r.getBranchId() != null ? r.getBranchId().value() : null))
                .orElse(true);
    }

    public void validateBranchAccess(UUID branchId) {
        if (!isAuthorizedForBranch(branchId)) {
            throw new AccessDeniedException("Unauthorized access for requested branch identifier: " + branchId);
        }
    }
}

