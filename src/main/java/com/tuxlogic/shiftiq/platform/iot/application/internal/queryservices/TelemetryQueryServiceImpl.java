package com.tuxlogic.shiftiq.platform.iot.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.iot.application.queryservices.TelemetryQueryService;
import com.tuxlogic.shiftiq.platform.iot.domain.model.aggregates.TelemetrySnapshot;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetLatestTelemetrySnapshotQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetTelemetrySnapshotHistoryQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetTelemetrySnapshotsByRegistrationIdQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetVehicleTelemetrySnapshotHistoryQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.repositories.Obd2DeviceRegistrationRepository;
import com.tuxlogic.shiftiq.platform.iot.domain.repositories.TelemetrySnapshotRepository;
import com.tuxlogic.shiftiq.platform.iot.domain.services.ActiveRegistrationContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for handling telemetry queries.
 */
@Service
public class TelemetryQueryServiceImpl implements TelemetryQueryService {

    private final TelemetrySnapshotRepository telemetrySnapshotRepository;
    private final Obd2DeviceRegistrationRepository obd2DeviceRegistrationRepository;
    private final ActiveRegistrationContextService activeRegistrationContextService;

    public TelemetryQueryServiceImpl(
            TelemetrySnapshotRepository telemetrySnapshotRepository,
            Obd2DeviceRegistrationRepository obd2DeviceRegistrationRepository,
            ActiveRegistrationContextService activeRegistrationContextService
    ) {
        this.telemetrySnapshotRepository = telemetrySnapshotRepository;
        this.obd2DeviceRegistrationRepository = obd2DeviceRegistrationRepository;
        this.activeRegistrationContextService = activeRegistrationContextService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TelemetrySnapshot> handle(GetLatestTelemetrySnapshotQuery query) {
        return obd2DeviceRegistrationRepository
                .findActiveByObd2DeviceId(query.obd2DeviceId())
                .flatMap(registration -> telemetrySnapshotRepository.findLatestByRegistrationId(registration.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TelemetrySnapshot> handle(GetTelemetrySnapshotHistoryQuery query) {
        return obd2DeviceRegistrationRepository
                .findActiveByObd2DeviceId(query.obd2DeviceId())
                .map(registration -> telemetrySnapshotRepository.findAllByRegistrationId(registration.getId(), query.page(), query.size()))
                .orElse(Collections.emptyList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TelemetrySnapshot> handle(GetTelemetrySnapshotsByRegistrationIdQuery query) {
        return telemetrySnapshotRepository.findAllByRegistrationId(query.obd2DeviceRegistrationId(), query.page(), query.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TelemetrySnapshot> handle(GetVehicleTelemetrySnapshotHistoryQuery query) {
        return activeRegistrationContextService.resolveActiveContextForVehicle(query.vehicleId())
                .map(ctx -> telemetrySnapshotRepository.findAllByRegistrationIdAndCreatedAtGreaterThanEqual(
                        ctx.obd2DeviceRegistrationId(),
                        ctx.startTimestamp(),
                        query.page(),
                        query.size()
                ))
                .orElseGet(List::of);
    }
}
