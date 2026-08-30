package com.andeva.atelier.platform.core.domain.repositories;

import com.andeva.atelier.platform.core.domain.model.aggregates.Branch;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.core.domain.model.valueobjects.WorkshopId;

import java.util.List;
import java.util.Optional;

public interface BranchRepository {
    Branch save(Branch branch);
    Optional<Branch> findById(BranchId id);
    List<Branch> findAllByWorkshopId(WorkshopId workshopId);
    boolean existsById(BranchId id);
    boolean existsByCode(String code);
}

