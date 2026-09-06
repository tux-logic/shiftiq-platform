package com.tuxlogic.shiftiq.platform.iot.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.iot.domain.model.aggregates.Vehicle;
import com.tuxlogic.shiftiq.platform.iot.domain.repositories.VehicleRepository;
import com.tuxlogic.shiftiq.platform.iot.infrastructure.persistence.jpa.assemblers.VehiclePersistenceAssembler;
import com.tuxlogic.shiftiq.platform.iot.infrastructure.persistence.jpa.entities.VehiclePersistenceEntity;
import com.tuxlogic.shiftiq.platform.iot.infrastructure.persistence.jpa.repositories.VehiclePersistenceRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class VehicleRepositoryImpl implements VehicleRepository {

    private final VehiclePersistenceRepository persistenceRepository;

    public VehicleRepositoryImpl(VehiclePersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Vehicle> findById(VehicleId id) {
        return persistenceRepository.findById(id.value())
                .map(VehiclePersistenceAssembler::toDomainEntity);
    }

    @Override
    @Transactional
    public Vehicle save(Vehicle vehicle) {
        VehiclePersistenceEntity entity = VehiclePersistenceAssembler.toPersistenceEntity(vehicle);
        VehiclePersistenceEntity savedEntity = persistenceRepository.save(entity);
        return VehiclePersistenceAssembler.toDomainEntity(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Vehicle> findByVin(String vin) {
        return persistenceRepository.findByVin(vin)
                .map(VehiclePersistenceAssembler::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Vehicle> findByPlateNumber(String plateNumber) {
        return persistenceRepository.findByPlateNumber(plateNumber)
                .map(VehiclePersistenceAssembler::toDomainEntity);
    }

    @Override
    @Transactional
    public void delete(VehicleId id) {
        persistenceRepository.deleteById(id.value());
    }
}