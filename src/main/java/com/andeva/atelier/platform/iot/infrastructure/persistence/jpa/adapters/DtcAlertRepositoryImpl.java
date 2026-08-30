package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.iot.domain.model.aggregates.DtcAlert;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.Obd2DeviceRegistrationId;
import com.andeva.atelier.platform.iot.domain.repositories.DtcAlertRepository;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.assemblers.DtcAlertPersistenceAssembler;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.repositories.DtcAlertPersistenceRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * JPA adapter implementing the DtcAlertRepository port.
 */
@Repository
public class DtcAlertRepositoryImpl implements DtcAlertRepository {

    private final DtcAlertPersistenceRepository persistenceRepository;

    public DtcAlertRepositoryImpl(DtcAlertPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public List<DtcAlert> findAllByRegistrationId(Obd2DeviceRegistrationId registrationId) {
        return persistenceRepository.findAllByRegistrationId(registrationId.value()).stream()
                .map(DtcAlertPersistenceAssembler::toDomainEntity)
                .toList();
    }

    @Override
    public List<DtcAlert> findAllByRegistrationIdAndCreatedAtGreaterThanEqual(
            Obd2DeviceRegistrationId registrationId,
            Instant startTimestamp
    ) {
        return persistenceRepository.findAllByRegistrationIdAndCreatedAtGreaterThanEqual(registrationId.value(), startTimestamp).stream()
                .map(DtcAlertPersistenceAssembler::toDomainEntity)
                .toList();
    }
}
