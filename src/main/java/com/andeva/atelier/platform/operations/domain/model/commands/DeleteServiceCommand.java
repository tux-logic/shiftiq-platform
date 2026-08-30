package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.operations.domain.model.valueobjects.ServiceId;

/**
 * Command to delete a service by its ID.
 * @param serviceId the unique identifier of the service to be deleted
 * @author Joel Huamani Estefanero
 */
public record DeleteServiceCommand(ServiceId serviceId) {
}
