package com.tuxlogic.shiftiq.platform.operations.application.outboundservices;

import java.util.UUID;

/**
 * Outbound service interface for interacting with employee/mechanic information from the core context.
 */
public interface ExternalEmployeeService {
    boolean existsEmployee(UUID employeeId);
}
