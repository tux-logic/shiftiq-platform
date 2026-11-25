package com.tuxlogic.shiftiq.platform.fleet.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.fleet.application.outboundservices.ExternalCoreService;
import com.tuxlogic.shiftiq.platform.fleet.application.queryservices.BranchAvailabilityQueryService;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.AppointmentStatus;
import com.tuxlogic.shiftiq.platform.fleet.domain.repositories.AppointmentRepository;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.resources.BranchAvailabilityResource;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BranchAvailabilityQueryServiceImpl implements BranchAvailabilityQueryService {

    private static final List<LocalTime> SLOT_STARTS = List.of(
            LocalTime.of(8, 30),
            LocalTime.of(10, 0),
            LocalTime.of(11, 30),
            LocalTime.of(14, 0),
            LocalTime.of(15, 30),
            LocalTime.of(17, 0)
    );

    private final ExternalCoreService externalCoreService;
    private final AppointmentRepository appointmentRepository;

    public BranchAvailabilityQueryServiceImpl(
            ExternalCoreService externalCoreService,
            AppointmentRepository appointmentRepository
    ) {
        this.externalCoreService = externalCoreService;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public BranchAvailabilityResource getAvailability(UUID branchId, LocalDate date) {
        List<UUID> activeBayIds = externalCoreService.findActiveBayIdsByBranchId(branchId);
        int activeBaysCount = activeBayIds.size();

        LocalDateTime nowStart = LocalDateTime.now();
        LocalDateTime nowEnd = nowStart.plusHours(1);
        Set<UUID> busyNow = findBusyBayIds(branchId, activeBayIds, nowStart, nowEnd);
        int freeBaysNow = Math.max(0, activeBaysCount - busyNow.size());

        List<BranchAvailabilityResource.AvailabilitySlotResource> slots = new ArrayList<>();
        for (LocalTime slotStart : SLOT_STARTS) {
            LocalDateTime start = LocalDateTime.of(date, slotStart);
            LocalDateTime end = start.plusHours(1);
            Set<UUID> busy = findBusyBayIds(branchId, activeBayIds, start, end);
            List<UUID> available = activeBayIds.stream()
                    .filter(bayId -> !busy.contains(bayId))
                    .toList();
            slots.add(new BranchAvailabilityResource.AvailabilitySlotResource(slotStart, slotStart.plusHours(1), available));
        }

        return new BranchAvailabilityResource(branchId, date, activeBaysCount, freeBaysNow, slots);
    }

    private Set<UUID> findBusyBayIds(UUID branchId, List<UUID> activeBayIds, LocalDateTime start, LocalDateTime end) {
        if (activeBayIds.isEmpty()) {
            return Set.of();
        }
        var branchIdVo = new BranchId(branchId);
        Set<UUID> busy = new HashSet<>();
        for (UUID bayId : activeBayIds) {
            boolean overlap = appointmentRepository
                    .existsByBranchIdAndBayIdAndStatusAndScheduledStartLessThanAndScheduledEndGreaterThan(
                            branchIdVo, bayId, AppointmentStatus.PENDING, end, start);
            if (overlap) {
                busy.add(bayId);
            }
        }
        return busy;
    }
}