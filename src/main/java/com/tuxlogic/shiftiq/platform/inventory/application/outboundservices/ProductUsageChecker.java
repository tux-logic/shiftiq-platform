package com.tuxlogic.shiftiq.platform.inventory.application.outboundservices;

import java.util.UUID;

/**
 * Outbound port used by Inventory to know whether a product is still referenced
 * by Operations work-order tasks (and therefore must not be deleted).
 */
public interface ProductUsageChecker {

    /**
     * @return true when at least one non-deleted work_order_task_products row
     * references the given product id.
     */
    boolean isReferencedByWorkOrderTasks(UUID productId);
}
