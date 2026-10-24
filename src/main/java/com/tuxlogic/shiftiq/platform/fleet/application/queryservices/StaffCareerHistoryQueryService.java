package com.tuxlogic.shiftiq.platform.fleet.application.queryservices;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.entities.StaffCareerSnapshot;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetStaffCareerHistoryByEmployeeAndBranchQuery;

import java.util.List;

public interface StaffCareerHistoryQueryService {
    List<StaffCareerSnapshot> handle(GetStaffCareerHistoryByEmployeeAndBranchQuery query);
}
