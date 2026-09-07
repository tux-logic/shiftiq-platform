package com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Owner;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.OwnerId;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.UserId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.OwnerRepository;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.assemblers.OwnerPersistenceAssembler;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.OwnerPersistenceEntity;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories.OwnerPersistenceRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class OwnerRepositoryImpl implements OwnerRepository {

    private final OwnerPersistenceRepository ownerPersistenceRepository;

    public OwnerRepositoryImpl(OwnerPersistenceRepository ownerPersistenceRepository) {
        this.ownerPersistenceRepository = ownerPersistenceRepository;
    }

    @Override
    @Transactional
    public Owner save(Owner owner) {
        var entity = JpaAdapterUtils.resolveEntity(
                owner.getId() != null ? owner.getId().value() : null,
                ownerPersistenceRepository::findById,
                OwnerPersistenceEntity::new
        );
        OwnerPersistenceAssembler.toEntity(owner, entity);
        return OwnerPersistenceAssembler.toDomain(ownerPersistenceRepository.save(entity));
    }

    @Override
    public Optional<Owner> findById(OwnerId id) {
        return ownerPersistenceRepository.findById(id.value()).map(OwnerPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<Owner> findByUserId(UserId userId) {
        return ownerPersistenceRepository.findByUserId(userId.value()).map(OwnerPersistenceAssembler::toDomain);
    }

    @Override
    public boolean existsByUserId(UserId userId) {
        return ownerPersistenceRepository.existsByUserId(userId.value());
    }

    @Override
    public Optional<Owner> findByDocumentNumber(String documentNumber) {
        return ownerPersistenceRepository.findByDocumentNumber(documentNumber).map(OwnerPersistenceAssembler::toDomain);
    }

    @Override
    public boolean existsById(OwnerId id) {
        return ownerPersistenceRepository.existsById(id.value());
    }

    @Override
    @Transactional
    public void delete(Owner owner) {
        if (owner == null || owner.getId() == null) {
            throw new IllegalArgumentException("Owner or Owner ID cannot be null for deletion");
        }
        ownerPersistenceRepository.deleteById(owner.getId().value());
    }
}
