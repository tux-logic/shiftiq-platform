package com.tuxlogic.shiftiq.platform.core.application.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface OperatingHoursService {

    record EffectiveSchedule(
            boolean open,
            LocalTime openTime,
            LocalTime closeTime,
            int slotDurationMinutes,
            String source,
            String reason
    ) {}

    record WeeklyHourDto(
            DayOfWeek dayOfWeek,
            boolean open,
            LocalTime openTime,
            LocalTime closeTime,
            int slotDurationMinutes,
            String source
    ) {}

    record WeeklyHourInput(
            DayOfWeek dayOfWeek,
            boolean open,
            LocalTime openTime,
            LocalTime closeTime,
            Integer slotDurationMinutes
    ) {}

    record ExceptionDto(
            UUID id,
            UUID workshopId,
            UUID branchId,
            LocalDate date,
            boolean open,
            LocalTime openTime,
            LocalTime closeTime,
            String reason
    ) {}

    record ExceptionInput(
            LocalDate date,
            boolean open,
            LocalTime openTime,
            LocalTime closeTime,
            String reason
    ) {}

    EffectiveSchedule getEffectiveScheduleForDate(UUID branchId, LocalDate date);

    List<WeeklyHourDto> getWorkshopWeeklySchedule(UUID workshopId);

    List<WeeklyHourDto> upsertWorkshopWeeklySchedule(UUID workshopId, List<WeeklyHourInput> items);

    List<WeeklyHourDto> getBranchEffectiveWeeklySchedule(UUID branchId);

    List<WeeklyHourDto> upsertBranchWeeklySchedule(UUID branchId, List<WeeklyHourInput> items);

    void resetBranchWeeklySchedule(UUID branchId);

    List<ExceptionDto> getWorkshopExceptions(UUID workshopId);

    ExceptionDto upsertWorkshopException(UUID workshopId, ExceptionInput input);

    void deleteWorkshopException(UUID workshopId, UUID exceptionId);

    List<ExceptionDto> getBranchExceptions(UUID branchId);

    ExceptionDto upsertBranchException(UUID branchId, ExceptionInput input);

    void deleteBranchException(UUID branchId, UUID exceptionId);
}
