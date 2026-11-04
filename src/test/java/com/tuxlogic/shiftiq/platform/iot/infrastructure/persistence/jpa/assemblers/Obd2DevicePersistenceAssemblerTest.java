package com.tuxlogic.shiftiq.platform.iot.infrastructure.persistence.jpa.assemblers;

import com.tuxlogic.shiftiq.platform.iot.domain.model.aggregates.Obd2Device;
import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.Obd2DeviceId;
import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.Obd2DeviceStatus;
import com.tuxlogic.shiftiq.platform.iot.infrastructure.persistence.jpa.entities.Obd2DevicePersistenceEntity;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class Obd2DevicePersistenceAssemblerTest {

    @Test
    @DisplayName("new domain OBD2 device with null version leaves id and version null for persistence generation (isNew = true)")
    void newDeviceLeavesIdNull() {
        var branchId = new BranchId(UUID.randomUUID());
        var macAddress = "AA:BB:CC:11:22:33";
        var domain = new Obd2Device(branchId, macAddress);

        var entity = Obd2DevicePersistenceAssembler.toPersistenceEntity(domain);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getVersion()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.isNew()).isTrue();
        assertThat(entity.getBranchId()).isEqualTo(branchId);
        assertThat(entity.getMacAddress()).isEqualTo(macAddress);
        assertThat(entity.getStatus()).isEqualTo(Obd2DeviceStatus.AVAILABLE.value());
    }

    @Test
    @DisplayName("existing domain OBD2 device with non-null version sets id and version")
    void existingDeviceSetsIdAndVersion() {
        var deviceId = Obd2DeviceId.random();
        var branchId = new BranchId(UUID.randomUUID());
        var macAddress = "AA:BB:CC:11:22:33";
        var lastPing = Instant.now();
        var domain = new Obd2Device(deviceId, branchId, macAddress, lastPing, Obd2DeviceStatus.LINKED, 1L);

        var existingEntity = new Obd2DevicePersistenceEntity();
        existingEntity.setCreatedAt(lastPing);

        var entity = Obd2DevicePersistenceAssembler.toPersistenceEntity(domain, existingEntity);

        assertThat(entity.getId()).isEqualTo(deviceId.value());
        assertThat(entity.getVersion()).isEqualTo(1L);
        assertThat(entity.isNew()).isFalse();
        assertThat(entity.getBranchId()).isEqualTo(branchId);
        assertThat(entity.getMacAddress()).isEqualTo(macAddress);
        assertThat(entity.getStatus()).isEqualTo(Obd2DeviceStatus.LINKED.value());
        assertThat(entity.getLastPing()).isEqualTo(lastPing);
    }
}
