package com.tuxlogic.shiftiq.platform.operations.application.outboundservices;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound service interface for interacting with employee/mechanic information from the core and fleet contexts.
 */
public interface ExternalEmployeeService {
    boolean existsEmployee(UUID employeeId);

    /**
     * Resolves candidateId to an active employee_registrations id in the given branch.
     * Supports both employee_registrations.id and employees.id (core profile id).
     *
     * @param candidateId registration ID or core employee profile ID
     * @param branchId target branch ID
     * @return resolved active employee registration ID, or candidateId as fallback
     */
    UUID resolveActiveRegistrationId(UUID candidateId, UUID branchId);

    /**
     * Fetches the full display name of a mechanic from either employee_registrations.id or employees.id.
     *
     * @param mechanicId registration ID or core employee profile ID
     * @return an Optional containing the mechanic's full name if found
     */
    Optional<String> fetchMechanicName(UUID mechanicId);
}

