package com.tuxlogic.shiftiq.platform.iot.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.iot.domain.model.aggregates.Obd2Device;
import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.Obd2DeviceId;
import com.tuxlogic.shiftiq.platform.iot.domain.repositories.Obd2DeviceRepository;
import com.tuxlogic.shiftiq.platform.iot.infrastructure.persistence.jpa.assemblers.Obd2DevicePersistenceAssembler;
import com.tuxlogic.shiftiq.platform.iot.infrastructure.persistence.jpa.entities.Obd2DevicePersistenceEntity;
import com.tuxlogic.shiftiq.platform.iot.infrastructure.persistence.jpa.repositories.Obd2DevicePersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.Obd2DeviceStatus;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import java.util.List;
import java.util.Optional;

@Repository
public class Obd2DeviceRepositoryImpl implements Obd2DeviceRepository {

    private final Obd2DevicePersistenceRepository persistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Obd2DeviceRepositoryImpl(
            Obd2DevicePersistenceRepository persistenceRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.persistenceRepository = persistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Obd2Device save(Obd2Device obd2Device) {
        Obd2DevicePersistenceEntity entity = Obd2DevicePersistenceAssembler.toPersistenceEntity(obd2Device);
        Obd2DevicePersistenceEntity savedEntity = persistenceRepository.save(entity);
        Obd2Device savedDevice = Obd2DevicePersistenceAssembler.toDomainEntity(savedEntity);

        if (savedDevice != null) {
            obd2Device.domainEvents().forEach(eventPublisher::publishEvent);
            obd2Device.clearDomainEvents();
        }
        return savedDevice;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Obd2Device> findById(Obd2DeviceId id) {
        return persistenceRepository.findById(id.value())
                .map(Obd2DevicePersistenceAssembler::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Obd2Device> findByMacAddress(String macAddress) {
        return persistenceRepository.findByMacAddress(macAddress)
                .map(Obd2DevicePersistenceAssembler::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByMacAddress(String macAddress) {
        return persistenceRepository.existsByMacAddress(macAddress);
    }

    @Override
    @Transactional
    public void delete(Obd2DeviceId id) {
        persistenceRepository.deleteById(id.value());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Obd2Device> findAllByBranchId(BranchId branchId) {
        return persistenceRepository.findAllByBranchId(branchId).stream()
                .map(Obd2DevicePersistenceAssembler::toDomainEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Obd2Device> findAllByBranchIdAndStatus(BranchId branchId, Obd2DeviceStatus status) {
        return persistenceRepository.findAllByBranchIdAndStatus(branchId, status.value()).stream()
                .map(Obd2DevicePersistenceAssembler::toDomainEntity)
                .toList();
    }
}