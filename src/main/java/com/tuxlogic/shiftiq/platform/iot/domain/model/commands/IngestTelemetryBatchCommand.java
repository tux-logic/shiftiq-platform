package com.tuxlogic.shiftiq.platform.iot.domain.model.commands;

import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.Obd2DeviceId;

import java.time.Instant;
import java.util.List;

/**
 * Command to ingest a batch of telemetry snapshots from an OBD2 device.
 */
public record IngestTelemetryBatchCommand(
        Obd2DeviceId obd2DeviceId,
        List<TelemetrySnapshotData> snapshots
) {
    public IngestTelemetryBatchCommand {
        if (obd2DeviceId == null) {
            throw new IllegalArgumentException("iot.error.command.obd2DeviceIdRequired");
        }
        if (snapshots == null || snapshots.isEmpty()) {
            throw new IllegalArgumentException("iot.error.command.snapshotsRequired");
        }
    }

    public record TelemetrySnapshotData(
            Integer rpm,
            Integer temperature,
            Double speedKmh,
            Integer odometerKm,
            Double fuelLevelPercent,
            Instant createdAt,
            List<DtcCodeData> dtcCodes
    ) {
        public TelemetrySnapshotData {
            if (rpm == null) {
                throw new IllegalArgumentException("iot.error.command.rpmRequired");
            }
            if (temperature == null) {
                throw new IllegalArgumentException("iot.error.command.temperatureRequired");
            }
            if (fuelLevelPercent == null) {
                throw new IllegalArgumentException("iot.error.command.fuelLevelRequired");
            }
        }
    }

    public record DtcCodeData(
            String dtcCode,
            String description,
            String severity
    ) {
        public DtcCodeData {
            if (dtcCode == null || dtcCode.isBlank()) {
                throw new IllegalArgumentException("iot.error.command.dtcCodeRequired");
            }
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("iot.error.command.dtcDescriptionRequired");
            }
            if (severity == null || severity.isBlank()) {
                throw new IllegalArgumentException("iot.error.command.dtcSeverityRequired");
            }
        }
    }
}