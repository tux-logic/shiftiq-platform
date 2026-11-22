package com.tuxlogic.shiftiq.platform.billing.application.outboundservices;

import java.util.List;
import java.util.UUID;

/**
 * Outbound ACL service interface allowing Billing to verify customer ownership and work order completion status.
 */
public interface ExternalBillingContextService {

    /**
     * Checks whether the authenticated IAM user owns the customer profile linked to the given work order.
     */
    boolean isCustomerOwnerOfWorkOrder(UUID userId, UUID workOrderId);

    /**
     * Checks whether the given work order has reached the COMPLETED status.
     */
    boolean isWorkOrderCompleted(UUID workOrderId);

    /**
     * Lists all non-deleted work order IDs belonging to the authenticated IAM user's customer profile.
     */
    List<UUID> findWorkOrderIdsByUserId(UUID userId);
}
