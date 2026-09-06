package com.tuxlogic.shiftiq.platform.operations.application.outboundservices;

import java.util.UUID;

/**
 * Outbound service interface for notifying billing context when work orders are completed.
 */
public interface ExternalBillingService {
    void notifyWorkOrderCompleted(UUID workOrderId, UUID branchId, Double totalAmount);
}
