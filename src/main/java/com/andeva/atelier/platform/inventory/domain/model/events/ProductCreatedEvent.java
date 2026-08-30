package com.andeva.atelier.platform.inventory.domain.model.events;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import java.util.UUID;

public record ProductCreatedEvent(Object source, BranchId branchId, UUID productId) {
}
