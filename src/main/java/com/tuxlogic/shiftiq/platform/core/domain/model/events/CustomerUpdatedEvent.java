package com.tuxlogic.shiftiq.platform.core.domain.model.events;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

/**
 * Domain event published when a Customer profile is updated.
 */
public class CustomerUpdatedEvent extends ApplicationEvent {
    private final UUID customerId;

    public CustomerUpdatedEvent(Object source, UUID customerId) {
        super(source);
        this.customerId = customerId;
    }

    public UUID getCustomerId() {
        return customerId;
    }
}
