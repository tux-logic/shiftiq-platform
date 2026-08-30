package com.tuxlogic.shiftiq.platform.core.domain.repositories;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Branch;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;

import java.util.List;
import java.util.Optional;

public interface BranchRepository {
    Branch save(Branch branch);
    Optional<Branch> findById(BranchId id);
    List<Branch> findAllByWorkshopId(WorkshopId workshopId);
    boolean existsById(BranchId id);
    boolean existsByCode(String code);
}

