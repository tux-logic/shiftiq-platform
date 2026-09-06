
package com.tuxlogic.shiftiq.platform.iot.domain.model.events;

import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.Obd2DeviceId;
import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.Obd2DeviceStatus;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

public record Obd2DeviceStatusChangedEvent(
        Obd2DeviceId obd2DeviceId,
        BranchId branchId,
        Obd2DeviceStatus newStatus
) {}