package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleRegistration;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.VehicleRegistrationStatus;
import com.andeva.atelier.platform.iot.domain.repositories.VehicleRegistrationRepository;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.assemblers.VehicleRegistrationPersistenceAssembler;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities.VehicleRegistrationPersistenceEntity;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.repositories.VehicleRegistrationPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA adapter implementing the VehicleRegistrationRepository port.
 */
@Repository
public class VehicleRegistrationRepositoryImpl implements VehicleRegistrationRepository {

    private final VehicleRegistrationPersistenceRepository persistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public VehicleRegistrationRepositoryImpl(
            VehicleRegistrationPersistenceRepository persistenceRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.persistenceRepository = persistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public VehicleRegistration save(VehicleRegistration registration) {
        VehicleRegistrationPersistenceEntity entity = VehicleRegistrationPersistenceAssembler.toPersistenceEntity(registration);
        VehicleRegistrationPersistenceEntity savedEntity = persistenceRepository.save(entity);
        VehicleRegistration savedRegistration = VehicleRegistrationPersistenceAssembler.toDomainEntity(savedEntity);

        if (savedRegistration != null) {
            registration.domainEvents().forEach(eventPublisher::publishEvent);
            registration.clearDomainEvents();
        }
        return savedRegistration;
    }

    @Override
    public Optional<VehicleRegistration> findActiveByVehicleId(VehicleId vehicleId) {
        return persistenceRepository.findByVehicleIdAndStatus(vehicleId, VehicleRegistrationStatus.ACTIVE.value())
                .map(VehicleRegistrationPersistenceAssembler::toDomainEntity);
    }

    @Override
    public List<VehicleRegistration> findAllActiveByUserId(UUID userId) {
        return persistenceRepository.findAllByUserIdAndStatus(userId, VehicleRegistrationStatus.ACTIVE.value()).stream()
                .map(VehicleRegistrationPersistenceAssembler::toDomainEntity)
                .toList();
    }
}
