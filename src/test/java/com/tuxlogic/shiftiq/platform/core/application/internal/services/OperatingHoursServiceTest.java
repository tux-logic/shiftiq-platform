package com.tuxlogic.shiftiq.platform.core.application.internal.services;

import com.tuxlogic.shiftiq.platform.core.application.services.OperatingHoursService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Branch;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.BranchRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.WorkshopRepository;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.OperatingHourExceptionPersistenceEntity;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.OperatingHourPersistenceEntity;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories.OperatingHourExceptionJpaRepository;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories.OperatingHourJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperatingHoursServiceTest {

    @Mock
    private OperatingHourJpaRepository operatingHourRepository;
    @Mock
    private OperatingHourExceptionJpaRepository exceptionRepository;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private WorkshopRepository workshopRepository;

    private OperatingHoursServiceImpl service;
    private UUID workshopId;
    private UUID branchId;

    @BeforeEach
    void setUp() {
        service = new OperatingHoursServiceImpl(operatingHourRepository, exceptionRepository, branchRepository, workshopRepository);
        workshopId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        Branch branch = org.mockito.Mockito.mock(Branch.class);
        org.mockito.Mockito.lenient().when(branch.getWorkshopId()).thenReturn(new WorkshopId(workshopId));
        org.mockito.Mockito.lenient().when(branchRepository.findById(new BranchId(branchId))).thenReturn(Optional.of(branch));
    }

    @Test
    void effectiveScheduleFollowsPrecedenceBranchExceptionThenWorkshopExceptionThenBranchThenWorkshop() {
        LocalDate holiday = LocalDate.of(2026, 7, 28); // Tuesday
        DayOfWeek day = holiday.getDayOfWeek();

        var workshopWeekly = new OperatingHourPersistenceEntity();
        workshopWeekly.setDayOfWeek(day);
        workshopWeekly.setOpen(true);
        workshopWeekly.setOpenTime(LocalTime.of(8, 30));
        workshopWeekly.setCloseTime(LocalTime.of(18, 0));
        workshopWeekly.setSlotDurationMinutes(60);

        var branchWeekly = new OperatingHourPersistenceEntity();
        branchWeekly.setDayOfWeek(day);
        branchWeekly.setOpen(true);
        branchWeekly.setOpenTime(LocalTime.of(9, 0));
        branchWeekly.setCloseTime(LocalTime.of(17, 0));
        branchWeekly.setSlotDurationMinutes(60);

        var workshopEx = new OperatingHourExceptionPersistenceEntity();
        workshopEx.setExceptionDate(holiday);
        workshopEx.setOpen(false);
        workshopEx.setReason("Fiestas Patrias");

        var branchEx = new OperatingHourExceptionPersistenceEntity();
        branchEx.setExceptionDate(holiday);
        branchEx.setOpen(true);
        branchEx.setOpenTime(LocalTime.of(9, 0));
        branchEx.setCloseTime(LocalTime.of(13, 0));
        branchEx.setReason("Medio dia feriado");

        when(operatingHourRepository.findByWorkshopIdAndBranchIdIsNullAndDayOfWeek(workshopId, day)).thenReturn(Optional.of(workshopWeekly));
        assertThat(service.getEffectiveScheduleForDate(branchId, holiday).source()).isEqualTo("WORKSHOP");

        when(operatingHourRepository.findByBranchIdAndDayOfWeek(branchId, day)).thenReturn(Optional.of(branchWeekly));
        assertThat(service.getEffectiveScheduleForDate(branchId, holiday).source()).isEqualTo("BRANCH");

        when(exceptionRepository.findByWorkshopIdAndBranchIdIsNullAndExceptionDate(workshopId, holiday)).thenReturn(Optional.of(workshopEx));
        var fromWorkshopEx = service.getEffectiveScheduleForDate(branchId, holiday);
        assertThat(fromWorkshopEx.source()).isEqualTo("WORKSHOP_EXCEPTION");
        assertThat(fromWorkshopEx.open()).isFalse();

        when(exceptionRepository.findByBranchIdAndExceptionDate(branchId, holiday)).thenReturn(Optional.of(branchEx));
        var fromBranchEx = service.getEffectiveScheduleForDate(branchId, holiday);
        assertThat(fromBranchEx.source()).isEqualTo("BRANCH_EXCEPTION");
        assertThat(fromBranchEx.open()).isTrue();
        assertThat(fromBranchEx.closeTime()).isEqualTo(LocalTime.of(13, 0));
    }

    @Test
    void upsertRejectsInvalidTimeWindowWhenOpen() {
        when(workshopRepository.findById(new WorkshopId(workshopId)))
                .thenReturn(Optional.of(org.mockito.Mockito.mock(com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Workshop.class)));

        assertThatThrownBy(() -> service.upsertWorkshopWeeklySchedule(workshopId, List.of(
                new OperatingHoursService.WeeklyHourInput(DayOfWeek.MONDAY, true, LocalTime.of(18, 0), LocalTime.of(8, 0), 60)
        ))).isInstanceOf(IllegalArgumentException.class);
    }
}
