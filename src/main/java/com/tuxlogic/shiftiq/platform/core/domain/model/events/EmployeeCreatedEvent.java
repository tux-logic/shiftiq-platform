package com.tuxlogic.shiftiq.platform.core.domain.model.events;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

/**
 * Domain event published when a new Employee profile is created.
 */
public class EmployeeCreatedEvent extends ApplicationEvent {
    private final UUID employeeId;
    private final UUID userId;

    public EmployeeCreatedEvent(Object source, UUID employeeId, UUID userId) {
        super(source);
        this.employeeId = employeeId;
        this.userId = userId;
    }

    public UUID getEmployeeId() {
        return employeeId;
    }

    public UUID getUserId() {
        return userId;
    }
}
