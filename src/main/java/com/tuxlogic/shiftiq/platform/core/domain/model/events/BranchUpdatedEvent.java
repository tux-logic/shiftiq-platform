package com.tuxlogic.shiftiq.platform.core.domain.model.events;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

/**
 * Domain event published when a Branch aggregate is updated.
 */
public class BranchUpdatedEvent extends ApplicationEvent {
    private final UUID branchId;

    public BranchUpdatedEvent(Object source, UUID branchId) {
        super(source);
        this.branchId = branchId;
    }

    public UUID getBranchId() {
        return branchId;
    }
}
