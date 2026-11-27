package com.tuxlogic.shiftiq.platform.fleet.application.outboundservices;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Anti-Corruption Layer (ACL) Outbound Port to verify entity existence in core context.
 */
public interface ExternalCoreService {

    record OperatingWindow(boolean open, LocalTime openTime, LocalTime closeTime, int slotDurationMinutes) {}

    boolean existsBranchById(BranchId branchId);
    boolean existsCustomerById(CustomerId customerId);
    boolean existsEmployeeById(EmployeeId employeeId);
    Optional<UUID> findUserIdByEmployeeId(EmployeeId employeeId);
    Optional<UUID> findWorkshopIdForBranch(BranchId branchId);
    boolean existsActiveWorkshopSpecialty(UUID workshopId, String code);

    boolean existsBranchBay(UUID bayId, UUID branchId);

    int countActiveBays(UUID branchId);

    java.util.List<UUID> findActiveBayIdsByBranchId(UUID branchId);

    default Optional<OperatingWindow> findEffectiveOperatingWindow(UUID branchId, LocalDate date) {
        return Optional.empty();
    }
}
