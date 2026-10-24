package com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.entities.StaffCareerSnapshot;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.StaffCareerHistoryRepository;
import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.assemblers.StaffCareerHistoryPersistenceAssembler;
import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.repositories.StaffCareerHistoryPersistenceRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class StaffCareerHistoryRepositoryAdapter implements StaffCareerHistoryRepository {

    private final StaffCareerHistoryPersistenceRepository persistenceRepository;

    public StaffCareerHistoryRepositoryAdapter(StaffCareerHistoryPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public StaffCareerSnapshot save(StaffCareerSnapshot snapshot) {
        var entity = StaffCareerHistoryPersistenceAssembler.toEntity(snapshot);
        var saved = persistenceRepository.save(entity);
        return StaffCareerHistoryPersistenceAssembler.toDomain(saved);
    }

    @Override
    public List<StaffCareerSnapshot> findByBranchIdAndEmployeeId(BranchId branchId, UUID employeeId) {
        if (branchId == null || employeeId == null) {
            return List.of();
        }
        return persistenceRepository.findByBranchIdAndEmployeeIdOrderByRecordedAtDesc(branchId.value(), employeeId)
                .stream()
                .map(StaffCareerHistoryPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<StaffCareerSnapshot> findByRegistrationId(UUID registrationId) {
        if (registrationId == null) {
            return List.of();
        }
        return persistenceRepository.findByRegistrationIdOrderByRecordedAtDesc(registrationId)
                .stream()
                .map(StaffCareerHistoryPersistenceAssembler::toDomain)
                .toList();
    }
}
