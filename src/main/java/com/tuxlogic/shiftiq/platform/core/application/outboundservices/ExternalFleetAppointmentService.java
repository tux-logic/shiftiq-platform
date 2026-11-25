package com.tuxlogic.shiftiq.platform.core.application.outboundservices;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * ACL outbound port to verify and query appointments from the fleet context.
 */
public interface ExternalFleetAppointmentService {

  boolean isCompletedAppointmentForCustomer(UUID appointmentId, UUID branchId, UUID customerId);

  Set<UUID> findPendingOccupiedBayIds(UUID branchId, LocalDateTime rangeStart, LocalDateTime rangeEnd);
}