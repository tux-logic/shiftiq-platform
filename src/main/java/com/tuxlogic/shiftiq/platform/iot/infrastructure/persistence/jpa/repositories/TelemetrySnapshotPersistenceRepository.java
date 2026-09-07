package com.tuxlogic.shiftiq.platform.iot.infrastructure.persistence.jpa.repositories;

import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.Obd2DeviceRegistrationId;
import com.tuxlogic.shiftiq.platform.iot.infrastructure.persistence.jpa.entities.TelemetrySnapshotPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for "telemetry_snapshots".
 */
@Repository
public interface TelemetrySnapshotPersistenceRepository extends JpaRepository<TelemetrySnapshotPersistenceEntity, UUID> {

    List<TelemetrySnapshotPersistenceEntity> findAllByObd2DeviceRegistrationIdOrderByCreatedAtDesc(
            Obd2DeviceRegistrationId obd2DeviceRegistrationId
    );

    List<TelemetrySnapshotPersistenceEntity> findAllByObd2DeviceRegistrationIdOrderByCreatedAtDesc(
            Obd2DeviceRegistrationId obd2DeviceRegistrationId,
            Pageable pageable
    );

    Optional<TelemetrySnapshotPersistenceEntity> findFirstByObd2DeviceRegistrationIdOrderByCreatedAtDesc(
            Obd2DeviceRegistrationId obd2DeviceRegistrationId
    );

    List<TelemetrySnapshotPersistenceEntity> findAllByObd2DeviceRegistrationIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            Obd2DeviceRegistrationId obd2DeviceRegistrationId,
            Instant createdAt
    );

    List<TelemetrySnapshotPersistenceEntity> findAllByObd2DeviceRegistrationIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            Obd2DeviceRegistrationId obd2DeviceRegistrationId,
            Instant createdAt,
            Pageable pageable
    );
}
