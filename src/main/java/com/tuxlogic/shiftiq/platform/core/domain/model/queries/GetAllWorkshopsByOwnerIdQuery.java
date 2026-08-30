package com.tuxlogic.shiftiq.platform.core.domain.model.queries;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.OwnerId;

public record GetAllWorkshopsByOwnerIdQuery(OwnerId ownerId) {
}
