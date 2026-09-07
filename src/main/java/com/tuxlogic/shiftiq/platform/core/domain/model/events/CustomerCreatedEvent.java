package com.tuxlogic.shiftiq.platform.core.domain.model.events;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

/**
 * Domain event published when a new Customer profile is created.
 */
public class CustomerCreatedEvent extends ApplicationEvent {
    private final UUID customerId;
    private final UUID userId;

    public CustomerCreatedEvent(Object source, UUID customerId, UUID userId) {
        super(source);
        this.customerId = customerId;
        this.userId = userId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getUserId() {
        return userId;
    }
}
