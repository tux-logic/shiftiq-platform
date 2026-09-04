package com.tuxlogic.shiftiq.platform.fleet.domain.repositories;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.Appointment;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.AppointmentStatus;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository {

    Appointment save(Appointment appointment);
    Optional<Appointment> findById(UUID appointmentId);
    boolean existsById(UUID appointmentId);
    void deleteById(UUID appointmentId);
    boolean existsOverlappingAppointment(BranchId branchId, VehicleId vehicleId,
            LocalDateTime scheduledEnd, LocalDateTime scheduledStart);
    boolean existsOverlappingAppointmentExcludingId(
            UUID appointmentId, BranchId branchId, VehicleId vehicleId, 
            LocalDateTime scheduledEnd, LocalDateTime scheduledStart);

    Page<Appointment> findByBranchId(BranchId branchId, Pageable pageable);
    Page<Appointment> findByCustomerId(CustomerId customerId, Pageable pageable);
    Page<Appointment> findByVehicleId(VehicleId vehicleId, Pageable pageable);
    Page<Appointment> findByBranchIdAndStatus(BranchId branchId, AppointmentStatus status, Pageable pageable);
}