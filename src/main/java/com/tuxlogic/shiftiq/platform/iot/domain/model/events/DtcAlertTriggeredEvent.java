package com.tuxlogic.shiftiq.platform.iot.domain.model.events;

import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.DtcAlertSeverity;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

import java.util.UUID;

/**
 * Domain event raised when a new DTC alert is generated from an incoming
 * telemetry batch.
 */
public record DtcAlertTriggeredEvent(
        UUID dtcAlertId,
        BranchId branchId,
        UUID telemetrySnapshotId,
        String dtcCode,
        DtcAlertSeverity severity
) {
}