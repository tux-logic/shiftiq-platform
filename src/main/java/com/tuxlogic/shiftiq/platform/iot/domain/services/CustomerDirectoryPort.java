package com.tuxlogic.shiftiq.platform.iot.domain.services;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Anti-corruption port that isolates `iot` from the concrete shape of
 * `core`/`fleet` when it needs to resolve which user drives which customer,
 * and which customers are registered in a branch.
 */
public interface CustomerDirectoryPort {
    Optional<UUID> findUserIdByCustomerId(UUID customerId);
    List<UUID> findUserIdsByCustomerIds(List<UUID> customerIds);
    List<UUID> findActiveCustomerIdsByBranchId(BranchId branchId);
}