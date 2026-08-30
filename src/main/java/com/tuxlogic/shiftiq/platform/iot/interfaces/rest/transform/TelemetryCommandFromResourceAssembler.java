package com.tuxlogic.shiftiq.platform.iot.interfaces.rest.transform;

import com.tuxlogic.shiftiq.platform.iot.domain.model.commands.IngestTelemetryBatchCommand;
import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.Obd2DeviceId;
import com.tuxlogic.shiftiq.platform.iot.interfaces.rest.resources.IngestTelemetryBatchResource;

import java.util.stream.Collectors;

public class TelemetryCommandFromResourceAssembler {

    public static IngestTelemetryBatchCommand toCommandFromResource(IngestTelemetryBatchResource resource) {
        if (resource == null) {
            return null;
        }

        var snapshotsData = resource.snapshots().stream()
                .map(data -> new IngestTelemetryBatchCommand.TelemetrySnapshotData(
                        data.rpm(),
                        data.temperature(),
                        data.speedKmh(),
                        data.odometerKm(),
                        data.fuelLevelPercent(),
                        data.createdAt()
                ))
                .collect(Collectors.toList());

        return new IngestTelemetryBatchCommand(
                new Obd2DeviceId(resource.obd2DeviceId()),
                snapshotsData
        );
    }
}
