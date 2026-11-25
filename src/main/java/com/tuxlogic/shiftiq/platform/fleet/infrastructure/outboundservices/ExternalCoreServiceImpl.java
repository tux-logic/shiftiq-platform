package com.tuxlogic.shiftiq.platform.fleet.infrastructure.outboundservices;

import com.tuxlogic.shiftiq.platform.core.application.queryservices.BranchQueryService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.CustomerQueryService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.EmployeeQueryService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.WorkshopSpecialtyQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BranchBayId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.BranchBayRepository;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetBranchByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetCustomerByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetEmployeeByIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.queries.GetWorkshopSpecialtiesByWorkshopIdQuery;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.fleet.application.outboundservices.ExternalCoreService;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ACL adapter delegating existence checks to the core context through its
 * application layer, so fleet never couples to core repositories.
 */
@Service
public class ExternalCoreServiceImpl implements ExternalCoreService {

    private final BranchQueryService branchQueryService;
    private final CustomerQueryService customerQueryService;
    private final EmployeeQueryService employeeQueryService;
    private final WorkshopSpecialtyQueryService workshopSpecialtyQueryService;
    private final BranchBayRepository branchBayRepository;

    public ExternalCoreServiceImpl(BranchQueryService branchQueryService,
                                   CustomerQueryService customerQueryService,
                                   EmployeeQueryService employeeQueryService,
                                   WorkshopSpecialtyQueryService workshopSpecialtyQueryService,
                                   BranchBayRepository branchBayRepository) {
        this.branchQueryService = branchQueryService;
        this.customerQueryService = customerQueryService;
        this.employeeQueryService = employeeQueryService;
        this.workshopSpecialtyQueryService = workshopSpecialtyQueryService;
        this.branchBayRepository = branchBayRepository;
    }

    @Override
    public boolean existsBranchById(BranchId branchId) {
        if (branchId == null) return false;
        return branchQueryService.handle(new GetBranchByIdQuery(branchId)).isPresent();
    }

    @Override
    public boolean existsCustomerById(CustomerId customerId) {
        if (customerId == null) return false;
        return customerQueryService.handle(new GetCustomerByIdQuery(customerId)).isPresent();
    }

    @Override
    public boolean existsEmployeeById(EmployeeId employeeId) {
        if (employeeId == null) return false;
        return employeeQueryService.handle(new GetEmployeeByIdQuery(employeeId)).isPresent();
    }

    @Override
    public Optional<UUID> findUserIdByEmployeeId(EmployeeId employeeId) {
        if (employeeId == null) return Optional.empty();
        return employeeQueryService.handle(new GetEmployeeByIdQuery(employeeId))
                .map(employee -> employee.getUserId().value());
    }

    @Override
    public Optional<UUID> findWorkshopIdForBranch(BranchId branchId) {
        if (branchId == null) return Optional.empty();
        return branchQueryService.handle(new GetBranchByIdQuery(branchId))
                .map(branch -> branch.getWorkshopId().value());
    }

    @Override
    public boolean existsActiveWorkshopSpecialty(UUID workshopId, String code) {
        if (workshopId == null || code == null || code.isBlank()) return false;
        String normalized = code.trim().toUpperCase();
        return workshopSpecialtyQueryService
                .handle(new GetWorkshopSpecialtiesByWorkshopIdQuery(new WorkshopId(workshopId), false))
                .stream()
                .anyMatch(specialty -> specialty.isActive() && normalized.equals(specialty.getCode()));
    }

    @Override
    public boolean existsBranchBay(UUID bayId, UUID branchId) {
        if (bayId == null || branchId == null) {
            return false;
        }
        return branchBayRepository.existsByIdAndBranchId(new BranchBayId(bayId), new BranchId(branchId));
    }

    @Override
    public int countActiveBays(UUID branchId) {
        if (branchId == null) {
            return 0;
        }
        return branchBayRepository.countActiveByBranchId(new BranchId(branchId));
    }

    @Override
    public List<UUID> findActiveBayIdsByBranchId(UUID branchId) {
        if (branchId == null) {
            return List.of();
        }
        return branchBayRepository.findAllActiveByBranchId(new BranchId(branchId)).stream()
                .map(bay -> bay.getId().value())
                .toList();
    }
}
