package com.tuxlogic.shiftiq.platform.fleet.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.fleet.application.queryservices.StaffCareerHistoryQueryService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.entities.StaffCareerSnapshot;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetStaffCareerHistoryByEmployeeAndBranchQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.StaffCareerHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class StaffCareerHistoryQueryServiceImpl implements StaffCareerHistoryQueryService {

    private final StaffCareerHistoryRepository repository;

    public StaffCareerHistoryQueryServiceImpl(StaffCareerHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<StaffCareerSnapshot> handle(GetStaffCareerHistoryByEmployeeAndBranchQuery query) {
        log.debug("Fetching staff career history for branch {} and employee {}",
                query.branchId(), query.employeeId());
        return repository.findByBranchIdAndEmployeeId(query.branchId(), query.employeeId());
    }
}
