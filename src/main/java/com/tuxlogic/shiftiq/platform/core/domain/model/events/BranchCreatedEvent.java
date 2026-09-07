package com.tuxlogic.shiftiq.platform.core.domain.model.events;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

/**
 * Domain event published when a new Branch aggregate is created.
 */
public class BranchCreatedEvent extends ApplicationEvent {
    private final UUID branchId;
    private final UUID workshopId;

    public BranchCreatedEvent(Object source, UUID branchId, UUID workshopId) {
        super(source);
        this.branchId = branchId;
        this.workshopId = workshopId;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public UUID getWorkshopId() {
        return workshopId;
    }
}
