package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.BranchBay;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.BranchBayId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.BranchBayRepository;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.assemblers.BranchBayPersistenceAssembler;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.BranchBayPersistenceEntity;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories.BranchBayPersistenceRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class BranchBayRepositoryImpl implements BranchBayRepository {

    private final BranchBayPersistenceRepository repository;

    public BranchBayRepositoryImpl(BranchBayPersistenceRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public BranchBay save(BranchBay bay) {
        var entity = JpaAdapterUtils.resolveEntity(
                bay.getId() != null ? bay.getId().value() : null,
                repository::findById,
                BranchBayPersistenceEntity::new
        );
        BranchBayPersistenceAssembler.toEntity(bay, entity);
        return BranchBayPersistenceAssembler.toDomain(repository.save(entity));
    }

    @Override
    public Optional<BranchBay> findById(BranchBayId id) {
        return repository.findById(id.value()).map(BranchBayPersistenceAssembler::toDomain);
    }

    @Override
    public List<BranchBay> findAllByBranchId(BranchId branchId) {
        return repository.findAllByBranchId(branchId.value()).stream()
                .map(BranchBayPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<BranchBay> findAllActiveByBranchId(BranchId branchId) {
        return repository.findAllByBranchIdAndActiveTrue(branchId.value()).stream()
                .map(BranchBayPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public boolean existsByBranchIdAndCode(BranchId branchId, String code) {
        return repository.existsByBranchIdAndCode(branchId.value(), code.trim().toUpperCase());
    }

    @Override
    public boolean existsByIdAndBranchId(BranchBayId bayId, BranchId branchId) {
        return repository.existsByIdAndBranchId(bayId.value(), branchId.value());
    }

    @Override
    public int countActiveByBranchId(BranchId branchId) {
        return repository.countByBranchIdAndActiveTrue(branchId.value());
    }
}