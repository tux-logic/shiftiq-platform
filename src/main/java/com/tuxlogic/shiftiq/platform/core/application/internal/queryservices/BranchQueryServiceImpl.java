package com.tuxlogic.shiftiq.platform.core.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.core.application.queryservices.BranchQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Branch;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetAllBranchesByWorkshopIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetBranchByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.BranchRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BranchQueryServiceImpl implements BranchQueryService {
    private final BranchRepository branchRepository;

    public BranchQueryServiceImpl(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    @Override
    public Optional<Branch> handle(GetBranchByIdQuery query) {
        return branchRepository.findById(query.id());
    }

    @Override
    public List<Branch> handle(GetAllBranchesByWorkshopIdQuery query) {
        return branchRepository.findAllByWorkshopId(query.workshopId());
    }
}
