package com.tuxlogic.shiftiq.platform.iot.domain.model.aggregates;

import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.Obd2DeviceId;
import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.Obd2DeviceStatus;
import com.tuxlogic.shiftiq.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.iot.domain.model.events.Obd2DeviceStatusChangedEvent;
import lombok.Getter;

import java.time.Instant;

/**
 * Domain Aggregate Root representing an OBD2 device registered in a branch.
 */
@Getter
public class Obd2Device extends AbstractDomainAggregateRoot<Obd2Device> {

    private Obd2DeviceId id;
    private BranchId branchId;
    private String macAddress;
    private Instant lastPing;
    private Obd2DeviceStatus status;
    private Long version;

    public Obd2Device() {
    }

    public Obd2Device(BranchId branchId, String macAddress) {
        if (branchId == null) {
            throw new IllegalArgumentException("iot.error.obd2Device.branchIdRequired");
        }
        if (macAddress == null || macAddress.isBlank()) {
            throw new IllegalArgumentException("iot.error.obd2Device.macAddressEmpty");
        }
        this.id = Obd2DeviceId.random();
        this.branchId = branchId;
        this.macAddress = macAddress;
        this.status = Obd2DeviceStatus.AVAILABLE;
        this.version = null;
    }

    public Obd2Device(Obd2DeviceId id, BranchId branchId, String macAddress, Instant lastPing, Obd2DeviceStatus status, Long version) {
        this.id = id;
        this.branchId = branchId;
        this.macAddress = macAddress;
        this.lastPing = lastPing;
        this.status = status;
        this.version = version;
    }

    public void ping() {
        this.lastPing = Instant.now();
    }

    public void markAsLinked() {
        if (Obd2DeviceStatus.LINKED.equals(this.status)) {
            throw new IllegalStateException("iot.error.obd2Device.alreadyLinked");
        }
        this.status = Obd2DeviceStatus.LINKED;
        registerDomainEvent(new Obd2DeviceStatusChangedEvent(this.id, this.branchId, this.status));
    }

    public void markAsAvailable() {
        this.status = Obd2DeviceStatus.AVAILABLE;
        registerDomainEvent(new Obd2DeviceStatusChangedEvent(this.id, this.branchId, this.status));
    }

    public void updateMacAddress(String macAddress) {
        if (macAddress == null || macAddress.isBlank()) {
            throw new IllegalArgumentException("iot.error.obd2Device.macAddressEmpty");
        }
        this.macAddress = macAddress;
    }
}