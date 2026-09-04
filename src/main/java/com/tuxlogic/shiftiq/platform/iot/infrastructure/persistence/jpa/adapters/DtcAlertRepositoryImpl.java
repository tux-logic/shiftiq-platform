package com.tuxlogic.shiftiq.platform.iot.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.iot.domain.model.aggregates.DtcAlert;
import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.Obd2DeviceRegistrationId;
import com.tuxlogic.shiftiq.platform.iot.domain.repositories.DtcAlertRepository;
import com.tuxlogic.shiftiq.platform.iot.infrastructure.persistence.jpa.assemblers.DtcAlertPersistenceAssembler;
import com.tuxlogic.shiftiq.platform.iot.infrastructure.persistence.jpa.entities.DtcAlertPersistenceEntity;
import com.tuxlogic.shiftiq.platform.iot.infrastructure.persistence.jpa.repositories.DtcAlertPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JPA adapter implementing the DtcAlertRepository port.
 */
@Repository
public class DtcAlertRepositoryImpl implements DtcAlertRepository {

    private final DtcAlertPersistenceRepository persistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public DtcAlertRepositoryImpl(
            DtcAlertPersistenceRepository persistenceRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.persistenceRepository = persistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public DtcAlert save(DtcAlert dtcAlert) {
        DtcAlertPersistenceEntity entity = DtcAlertPersistenceAssembler.toPersistenceEntity(dtcAlert);
        DtcAlertPersistenceEntity savedEntity = persistenceRepository.save(entity);
        DtcAlert savedAlert = DtcAlertPersistenceAssembler.toDomainEntity(savedEntity);

        if (savedAlert != null) {
            dtcAlert.domainEvents().forEach(eventPublisher::publishEvent);
            dtcAlert.clearDomainEvents();
        }
        return savedAlert;
    }

    @Override
    public List<DtcAlert> saveAll(List<DtcAlert> dtcAlerts) {
        List<DtcAlertPersistenceEntity> entities = dtcAlerts.stream()
                .map(DtcAlertPersistenceAssembler::toPersistenceEntity)
                .collect(Collectors.toList());

        List<DtcAlertPersistenceEntity> savedEntities = persistenceRepository.saveAll(entities);

        List<DtcAlert> savedAlerts = savedEntities.stream()
                .map(DtcAlertPersistenceAssembler::toDomainEntity)
                .collect(Collectors.toList());

        for (DtcAlert dtcAlert : dtcAlerts) {
            dtcAlert.domainEvents().forEach(eventPublisher::publishEvent);
            dtcAlert.clearDomainEvents();
        }
        return savedAlerts;
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