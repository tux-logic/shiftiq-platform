package com.tuxlogic.shiftiq.platform.iot.domain.model.commands;

import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;

/**
 * Command to register a new OBD2 device.
 */
public record CreateObd2DeviceCommand(
        BranchId branchId,
        String macAddress
) {
    public CreateObd2DeviceCommand {
        if (branchId == null) {
            throw new IllegalArgumentException("iot.error.command.branchIdRequired");
        }
        if (macAddress == null || macAddress.isBlank()) {
            throw new IllegalArgumentException("iot.error.command.macAddressRequired");
        }
    }
}