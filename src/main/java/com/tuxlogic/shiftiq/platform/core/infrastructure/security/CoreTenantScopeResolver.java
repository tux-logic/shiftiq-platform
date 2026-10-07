package com.tuxlogic.shiftiq.platform.core.infrastructure.security;

import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.BranchPersistenceEntity;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.WorkshopPersistenceEntity;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories.BranchPersistenceRepository;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories.OwnerPersistenceRepository;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories.WorkshopPersistenceRepository;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.TenantScopeResolver;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Core implementation of {@link TenantScopeResolver}: user -&gt; owner -&gt; workshops -&gt; branches.
 *
 * <p>Every lookup fails closed, so a user without an owner profile or an owner without
 * workshops resolves to an empty scope instead of granting access.</p>
 */
@Component
public class CoreTenantScopeResolver implements TenantScopeResolver {

    private final OwnerPersistenceRepository ownerRepository;
    private final WorkshopPersistenceRepository workshopRepository;
    private final BranchPersistenceRepository branchRepository;

    public CoreTenantScopeResolver(OwnerPersistenceRepository ownerRepository,
                                   WorkshopPersistenceRepository workshopRepository,
                                   BranchPersistenceRepository branchRepository) {
        this.ownerRepository = ownerRepository;
        this.workshopRepository = workshopRepository;
        this.branchRepository = branchRepository;
    }

    @Override
    public Set<UUID> findWorkshopIdsForUser(UUID userId) {
        if (userId == null) {
            return Set.of();
        }
        return ownerRepository.findByUserId(userId)
                .map(owner -> workshopRepository.findAllByOwnerId(owner.getId()))
                .map(workshops -> workshops.stream()
                        .map(WorkshopPersistenceEntity::getId)
                        .collect(Collectors.toSet()))
                .orElseGet(Set::of);
    }

    @Override
    public Set<UUID> findBranchIdsForUser(UUID userId) {
        return findWorkshopIdsForUser(userId).stream()
                .flatMap(workshopId -> branchRepository.findAllByWorkshopId(workshopId).stream())
                .map(BranchPersistenceEntity::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public UUID findOwnerProfileIdForUser(UUID userId) {
        if (userId == null) {
            return null;
        }
        return ownerRepository.findByUserId(userId).map(owner -> owner.getId()).orElse(null);
    }

    @Override
    public Set<UUID> findAllBranchIds() {
        return branchRepository.findAll().stream()
                .map(BranchPersistenceEntity::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean branchExists(UUID branchId) {
        return branchId != null && branchRepository.existsById(branchId);
    }

    @Override
    public UUID findWorkshopIdForBranch(UUID branchId) {
        if (branchId == null) {
            return null;
        }
        return branchRepository.findById(branchId)
                .map(BranchPersistenceEntity::getWorkshopId)
                .orElse(null);
    }

    @Override
    public int countBranchesForWorkshop(UUID workshopId) {
        if (workshopId == null) {
            return 0;
        }
        return branchRepository.findAllByWorkshopId(workshopId).size();
    }
}
