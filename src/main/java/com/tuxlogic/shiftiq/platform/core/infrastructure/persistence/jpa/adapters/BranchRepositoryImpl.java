package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Branch;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.BranchRepository;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.assemblers.BranchPersistenceAssembler;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.BranchPersistenceEntity;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories.BranchPersistenceRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class BranchRepositoryImpl implements BranchRepository {

    private final BranchPersistenceRepository branchPersistenceRepository;

    public BranchRepositoryImpl(BranchPersistenceRepository branchPersistenceRepository) {
        this.branchPersistenceRepository = branchPersistenceRepository;
    }

    @Override
    @Transactional
    public Branch save(Branch branch) {
        var entity = JpaAdapterUtils.resolveEntity(
                branch.getId() != null ? branch.getId().value() : null,
                branchPersistenceRepository::findById,
                BranchPersistenceEntity::new
        );
        BranchPersistenceAssembler.toEntity(branch, entity);
        return BranchPersistenceAssembler.toDomain(branchPersistenceRepository.save(entity));
    }

    @Override
    public Optional<Branch> findById(BranchId id) {
        return branchPersistenceRepository.findById(id.value()).map(BranchPersistenceAssembler::toDomain);
    }

    @Override
    public List<Branch> findAllByWorkshopId(WorkshopId workshopId) {
        return branchPersistenceRepository.findAllByWorkshopId(workshopId.value()).stream()
                .map(BranchPersistenceAssembler::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(BranchId id) {
        return branchPersistenceRepository.existsById(id.value());
    }

    @Override
    public boolean existsByCode(String code) {
        return branchPersistenceRepository.existsByCode(code);
    }
}

