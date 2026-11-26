package com.tuxlogic.shiftiq.platform.core.domain.repositories;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.BranchBay;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BranchBayId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import java.util.List;
import java.util.Optional;

public interface BranchBayRepository {
    BranchBay save(BranchBay bay);
    Optional<BranchBay> findById(BranchBayId id);
    List<BranchBay> findAllByBranchId(BranchId branchId);
    List<BranchBay> findAllActiveByBranchId(BranchId branchId);
    boolean existsByBranchIdAndCode(BranchId branchId, String code);
    boolean existsByIdAndBranchId(BranchBayId bayId, BranchId branchId);
    int countActiveByBranchId(BranchId branchId);
}