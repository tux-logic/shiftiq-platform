package com.tuxlogic.shiftiq.platform.core.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.core.application.queryservices.BranchCatalogItem;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.BranchQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Branch;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Workshop;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetAllBranchesByWorkshopIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetBranchByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.BranchRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.WorkshopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BranchQueryServiceImpl implements BranchQueryService {
    private final BranchRepository branchRepository;
    private final WorkshopRepository workshopRepository;

    public BranchQueryServiceImpl(BranchRepository branchRepository, WorkshopRepository workshopRepository) {
        this.branchRepository = branchRepository;
        this.workshopRepository = workshopRepository;
    }

    @Override
    public Optional<Branch> handle(GetBranchByIdQuery query) {
        return branchRepository.findById(query.id());
    }

    @Override
    public List<Branch> handle(GetAllBranchesByWorkshopIdQuery query) {
        return branchRepository.findAllByWorkshopId(query.workshopId());
    }

    @Override
    public List<BranchCatalogItem> findCatalog() {
        Map<UUID, Workshop> workshopsById = workshopRepository.findAll().stream()
                .filter(workshop -> workshop.getId() != null)
                .collect(Collectors.toMap(
                        workshop -> workshop.getId().value(),
                        Function.identity(),
                        (left, right) -> left
                ));

        return branchRepository.findAll().stream()
                .map(branch -> toCatalogItem(branch, workshopsById.get(
                        branch.getWorkshopId() != null ? branch.getWorkshopId().value() : null)))
                .toList();
    }

    private BranchCatalogItem toCatalogItem(Branch branch, Workshop workshop) {
        return new BranchCatalogItem(
                branch.getId() != null ? branch.getId().value() : null,
                branch.getWorkshopId() != null ? branch.getWorkshopId().value() : null,
                workshop != null ? workshop.getBrandName() : null,
                workshop != null ? workshop.getBusinessName() : null,
                branch.getCode(),
                branch.getName(),
                branch.getAddress() != null ? branch.getAddress().value() : null,
                branch.getPhone() != null ? branch.getPhone().value() : null
        );
    }
}
