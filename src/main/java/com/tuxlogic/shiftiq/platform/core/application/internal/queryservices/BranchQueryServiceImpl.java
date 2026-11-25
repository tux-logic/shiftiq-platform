package com.tuxlogic.shiftiq.platform.core.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.core.application.outboundservices.ExternalFleetAppointmentService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.BranchCatalogItem;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.BranchQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Branch;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Workshop;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetAllBranchesByWorkshopIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetBranchByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.BranchBayRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.BranchRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.BranchReviewRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.WorkshopRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final BranchBayRepository branchBayRepository;
    private final BranchReviewRepository branchReviewRepository;
    private final ExternalFleetAppointmentService externalFleetAppointmentService;

    public BranchQueryServiceImpl(
            BranchRepository branchRepository,
            WorkshopRepository workshopRepository,
            BranchBayRepository branchBayRepository,
            BranchReviewRepository branchReviewRepository,
            ExternalFleetAppointmentService externalFleetAppointmentService
    ) {
        this.branchRepository = branchRepository;
        this.workshopRepository = workshopRepository;
        this.branchBayRepository = branchBayRepository;
        this.branchReviewRepository = branchReviewRepository;
        this.externalFleetAppointmentService = externalFleetAppointmentService;
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

        LocalDateTime now = LocalDateTime.now();
        return branchRepository.findAll().stream()
                .map(branch -> toCatalogItem(branch, workshopsById.get(
                        branch.getWorkshopId() != null ? branch.getWorkshopId().value() : null), now))
                .toList();
    }

    private BranchCatalogItem toCatalogItem(Branch branch, Workshop workshop, LocalDateTime now) {
        BranchId branchId = branch.getId();
        Double averageRating = branchId != null
                ? branchReviewRepository.averageRatingByBranchId(branchId)
                : null;
        int reviewCount = branchId != null ? branchReviewRepository.countByBranchId(branchId) : 0;
        int activeBaysCount = branchId != null ? branchBayRepository.countActiveByBranchId(branchId) : 0;
        int occupiedNow = 0;
        if (branchId != null && activeBaysCount > 0) {
            occupiedNow = externalFleetAppointmentService.findPendingOccupiedBayIds(
                    branchId.value(), now, now.plusHours(1)).size();
        }
        int freeBaysToday = Math.max(0, activeBaysCount - occupiedNow);

        return new BranchCatalogItem(
                branch.getId() != null ? branch.getId().value() : null,
                branch.getWorkshopId() != null ? branch.getWorkshopId().value() : null,
                workshop != null ? workshop.getBrandName() : null,
                workshop != null ? workshop.getBusinessName() : null,
                branch.getCode(),
                branch.getName(),
                branch.getAddress() != null ? branch.getAddress().value() : null,
                branch.getPhone() != null ? branch.getPhone().value() : null,
                averageRating,
                reviewCount,
                activeBaysCount,
                freeBaysToday
        );
    }
}