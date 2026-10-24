package com.tuxlogic.shiftiq.platform.core.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.core.application.queryservices.WorkshopSpecialtyHistoryQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.entities.WorkshopSpecialtyRevision;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetWorkshopSpecialtyHistoryQuery;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.WorkshopSpecialtyHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class WorkshopSpecialtyHistoryQueryServiceImpl implements WorkshopSpecialtyHistoryQueryService {

    private final WorkshopSpecialtyHistoryRepository repository;

    public WorkshopSpecialtyHistoryQueryServiceImpl(WorkshopSpecialtyHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<WorkshopSpecialtyRevision> handle(GetWorkshopSpecialtyHistoryQuery query) {
        log.debug("Fetching specialty history for workshop {} and specialty {}",
                query.workshopId(), query.specialtyId());
        if (query.specialtyId() != null) {
            return repository.findByWorkshopIdAndSpecialtyId(query.workshopId(), query.specialtyId());
        }
        return repository.findByWorkshopId(query.workshopId());
    }
}
