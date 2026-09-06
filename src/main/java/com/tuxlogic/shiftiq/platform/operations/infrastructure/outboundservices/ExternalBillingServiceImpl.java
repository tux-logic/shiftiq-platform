package com.tuxlogic.shiftiq.platform.operations.infrastructure.outboundservices;

import com.tuxlogic.shiftiq.platform.operations.application.outboundservices.ExternalBillingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * ACL adapter for sending notification events to the billing context when a work order completes.
 */
@Service
public class ExternalBillingServiceImpl implements ExternalBillingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalBillingServiceImpl.class);

    @Override
    public void notifyWorkOrderCompleted(UUID workOrderId, UUID branchId, Double totalAmount) {
        LOGGER.info("Outbound notification sent to Billing: WorkOrder {} completed at Branch {} with total amount {}",
                workOrderId, branchId, totalAmount);
    }
}
