package com.tuxlogic.shiftiq.platform.core.infrastructure.outboundservices;

import com.tuxlogic.shiftiq.platform.core.application.outboundservices.ExternalFleetAppointmentService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.AppointmentStatus;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.AppointmentRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ExternalFleetAppointmentServiceImpl implements ExternalFleetAppointmentService {

    private final AppointmentRepository appointmentRepository;

    public ExternalFleetAppointmentServiceImpl(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public boolean isCompletedAppointmentForCustomer(UUID appointmentId, UUID branchId, UUID customerId) {
        var appointment = appointmentRepository.findById(appointmentId);
        return appointment
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .filter(a -> a.getBranchId().value().equals(branchId))
                .filter(a -> a.getCustomerId().value().equals(customerId))
                .isPresent();
    }

    @Override
    public Set<UUID> findPendingOccupiedBayIds(UUID branchId, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        var branchIdVo = new BranchId(branchId);
        var occupied = new HashSet<UUID>();
        for (var appointment : appointmentRepository.findByBranchId(branchIdVo)) {
            if (appointment.getStatus() != AppointmentStatus.PENDING) {
                continue;
            }
            UUID bayId = appointment.getBayId();
            if (bayId == null) {
                continue;
            }
            if (appointment.getScheduledStart().isBefore(rangeEnd)
                    && appointment.getScheduledEnd().isAfter(rangeStart)) {
                occupied.add(bayId);
            }
        }
        return occupied;
    }
}