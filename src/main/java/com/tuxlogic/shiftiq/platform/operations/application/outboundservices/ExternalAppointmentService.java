package com.tuxlogic.shiftiq.platform.operations.application.outboundservices;

import java.util.UUID;

/**
 * Outbound service interface for interacting with appointment information from the fleet context.
 */
public interface ExternalAppointmentService {
    boolean existsAppointment(UUID appointmentId);
}
