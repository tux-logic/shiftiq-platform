package com.tuxlogic.shiftiq.platform.core.domain.model.events;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

/**
 * Domain event published when an Employee profile is updated.
 */
public class EmployeeUpdatedEvent extends ApplicationEvent {
    private final UUID employeeId;

    public EmployeeUpdatedEvent(Object source, UUID employeeId) {
        super(source);
        this.employeeId = employeeId;
    }

    public UUID getEmployeeId() {
        return employeeId;
    }
}
