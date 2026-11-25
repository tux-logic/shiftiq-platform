package com.tuxlogic.shiftiq.platform.core.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.core.application.queryservices.BranchBayQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.BranchBay;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetBranchBayByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetBranchBaysByBranchIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.BranchBayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class BranchBayQueryServiceImpl implements BranchBayQueryService {

    private final BranchBayRepository bayRepository;

    public BranchBayQueryServiceImpl(BranchBayRepository bayRepository) {
        this.bayRepository = bayRepository;
    }

    @Override
    public List<BranchBay> handle(GetBranchBaysByBranchIdQuery query) {
        if (query.activeOnly()) {
            return bayRepository.findAllActiveByBranchId(query.branchId());
        }
        return bayRepository.findAllByBranchId(query.branchId());
    }

    @Override
    public Optional<BranchBay> handle(GetBranchBayByIdQuery query) {
        return bayRepository.findById(query.bayId());
    }
}