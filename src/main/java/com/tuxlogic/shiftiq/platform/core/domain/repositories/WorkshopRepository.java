package com.tuxlogic.shiftiq.platform.core.domain.repositories;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Workshop;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.OwnerId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;

import java.util.List;
import java.util.Optional;

public interface WorkshopRepository {
    Workshop save(Workshop workshop);
    Optional<Workshop> findById(WorkshopId id);
    List<Workshop> findAllByOwnerId(OwnerId ownerId);
    boolean existsById(WorkshopId id);
}
