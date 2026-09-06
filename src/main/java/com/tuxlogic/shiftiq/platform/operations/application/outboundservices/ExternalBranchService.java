package com.tuxlogic.shiftiq.platform.operations.application.outboundservices;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound service interface for fetching branch code information.
 */
public interface ExternalBranchService {
    Optional<String> fetchBranchCode(UUID branchId);
}
