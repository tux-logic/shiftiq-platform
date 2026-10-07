package com.tuxlogic.shiftiq.platform.core.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.core.application.queryservices.WorkshopSpecialtyQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.WorkshopSpecialty;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetWorkshopSpecialtiesByWorkshopIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetWorkshopSpecialtyByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.WorkshopSpecialtyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class WorkshopSpecialtyQueryServiceImpl implements WorkshopSpecialtyQueryService {

    private final WorkshopSpecialtyRepository specialtyRepository;

    public WorkshopSpecialtyQueryServiceImpl(WorkshopSpecialtyRepository specialtyRepository) {
        this.specialtyRepository = specialtyRepository;
    }

    @Override
    public List<WorkshopSpecialty> handle(GetWorkshopSpecialtiesByWorkshopIdQuery query) {
        if (query.activeOnly()) {
            return specialtyRepository.findAllActiveByWorkshopId(query.workshopId());
        }
        return specialtyRepository.findAllByWorkshopId(query.workshopId());
    }

    @Override
    public Optional<WorkshopSpecialty> handle(GetWorkshopSpecialtyByIdQuery query) {
        return specialtyRepository.findById(query.specialtyId());
    }
}
