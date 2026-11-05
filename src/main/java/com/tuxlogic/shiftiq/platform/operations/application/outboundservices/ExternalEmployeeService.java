package com.tuxlogic.shiftiq.platform.operations.application.outboundservices;

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
}

