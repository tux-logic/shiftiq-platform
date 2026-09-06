package com.tuxlogic.shiftiq.platform.iot.interfaces.rest.resources;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Resource representing the request payload to ingest a batch of telemetry snapshots.
 */
public record IngestTelemetryBatchResource(
        @NotNull(message = "iot.error.resource.obd2DeviceId.required")
        UUID obd2DeviceId,

        @NotNull(message = "iot.error.resource.snapshots.required")
        @Valid
        List<TelemetrySnapshotDataResource> snapshots
) {
    public record TelemetrySnapshotDataResource(
            @NotNull(message = "iot.error.resource.rpm.required")
            Integer rpm,

            @NotNull(message = "iot.error.resource.temperature.required")
            Integer temperature,

            Double speedKmh,
            Integer odometerKm,

            @NotNull(message = "iot.error.resource.fuelLevelPercent.required")
            Double fuelLevelPercent,

            Instant createdAt,

            @Valid
            List<DtcCodeDataResource> dtcCodes
    ) {}

    public record DtcCodeDataResource(
            @NotBlank(message = "iot.error.resource.dtcCode.required")
            String dtcCode,

            @NotBlank(message = "iot.error.resource.dtcDescription.required")
            String description,

            @NotBlank(message = "iot.error.resource.dtcSeverity.required")
            String severity
    ) {}
}