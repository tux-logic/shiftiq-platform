package com.tuxlogic.shiftiq.platform.iot.domain.model.aggregates;

import com.tuxlogic.shiftiq.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain Aggregate Root representing a Vehicle within the iot bounded context.
 */
public class Vehicle extends AbstractDomainAggregateRoot<Vehicle> {

    @Getter private VehicleId id;
    @Getter private String plateNumber;
    @Getter private String brand;
    @Getter private String model;
    @Getter private Integer year;
    @Getter private String vin;
    @Getter(lombok.AccessLevel.PACKAGE) private Long version;
    @Getter private Instant createdAt;
    @Getter private Instant updatedAt;
    @Getter(lombok.AccessLevel.PACKAGE) private Instant deletedAt;

    public Vehicle() {
    }

    public Vehicle(String plateNumber, String brand, String model, Integer year, String vin) {
        validate(plateNumber, brand, model, year, vin);
        this.id = new VehicleId(UUID.randomUUID());
        this.plateNumber = plateNumber;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.vin = vin;
        this.createdAt = Instant.now();
    }

    public Vehicle(
            VehicleId id,
            String plateNumber,
            String brand,
            String model,
            Integer year,
            String vin,
            Long version,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = id;
        this.plateNumber = plateNumber;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.vin = vin;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    /**
     * Updates the vehicle details.
     */
    public void updateDetails(String plateNumber, String brand, String model, Integer year, String vin) {
        validate(plateNumber, brand, model, year, vin);
        this.plateNumber = plateNumber;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.vin = vin;
        this.updatedAt = Instant.now();
        registerDomainEvent(new com.tuxlogic.shiftiq.platform.iot.domain.model.events.VehicleDetailsUpdatedEvent(
                this.id, this.plateNumber, this.brand, this.model, this.year, this.vin
        ));
    }

    private void validate(String plateNumber, String brand, String model, Integer year, String vin) {
        if (plateNumber == null || plateNumber.isBlank()) {
            throw new IllegalArgumentException("iot.error.vehicle.plateNumberRequired");
        }
        if (brand == null || brand.isBlank()) {
            throw new IllegalArgumentException("iot.error.vehicle.brandRequired");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("iot.error.vehicle.modelRequired");
        }
        if (year == null || year < 1886) {
            throw new IllegalArgumentException("iot.error.vehicle.yearInvalid");
        }
        if (vin == null || vin.isBlank()) {
            throw new IllegalArgumentException("iot.error.vehicle.vinRequired");
        }
    }
}