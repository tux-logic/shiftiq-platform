package com.tuxlogic.shiftiq.platform.core.application.internal.services;

import com.tuxlogic.shiftiq.platform.core.application.services.OperatingHoursService;
import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.BranchRepository;
import com.tuxlogic.shiftiq.platform.core.domain.repositories.WorkshopRepository;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.OperatingHourExceptionPersistenceEntity;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.entities.OperatingHourPersistenceEntity;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories.OperatingHourExceptionJpaRepository;
import com.tuxlogic.shiftiq.platform.core.infrastructure.persistence.jpa.repositories.OperatingHourJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OperatingHoursServiceImpl implements OperatingHoursService {

    private static final LocalTime DEFAULT_OPEN = LocalTime.of(8, 30);
    private static final LocalTime DEFAULT_CLOSE = LocalTime.of(18, 0);
    private static final int DEFAULT_SLOT_MINUTES = 60;

    private final OperatingHourJpaRepository operatingHourRepository;
    private final OperatingHourExceptionJpaRepository exceptionRepository;
    private final BranchRepository branchRepository;
    private final WorkshopRepository workshopRepository;

    public OperatingHoursServiceImpl(
            OperatingHourJpaRepository operatingHourRepository,
            OperatingHourExceptionJpaRepository exceptionRepository,
            BranchRepository branchRepository,
            WorkshopRepository workshopRepository
    ) {
        this.operatingHourRepository = operatingHourRepository;
        this.exceptionRepository = exceptionRepository;
        this.branchRepository = branchRepository;
        this.workshopRepository = workshopRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public EffectiveSchedule getEffectiveScheduleForDate(UUID branchId, LocalDate date) {
        if (branchId == null || date == null) {
            throw new IllegalArgumentException("core.error.operatingHours.invalidQuery");
        }
        var branch = branchRepository.findById(new BranchId(branchId))
                .orElseThrow(() -> new IllegalArgumentException("core.error.branch.notFound"));
        UUID workshopId = branch.getWorkshopId() != null ? branch.getWorkshopId().value() : null;

        // 1. Branch-level date exception
        var branchException = exceptionRepository.findByBranchIdAndExceptionDate(branchId, date);
        if (branchException.isPresent()) {
            var ex = branchException.get();
            int slotMinutes = resolveSlotDuration(branchId, workshopId, date.getDayOfWeek());
            return new EffectiveSchedule(ex.isOpen(), ex.getOpenTime(), ex.getCloseTime(), slotMinutes, "BRANCH_EXCEPTION", ex.getReason());
        }

        // 2. Workshop-level date exception
        if (workshopId != null) {
            var workshopException = exceptionRepository.findByWorkshopIdAndBranchIdIsNullAndExceptionDate(workshopId, date);
            if (workshopException.isPresent()) {
                var ex = workshopException.get();
                int slotMinutes = resolveSlotDuration(branchId, workshopId, date.getDayOfWeek());
                return new EffectiveSchedule(ex.isOpen(), ex.getOpenTime(), ex.getCloseTime(), slotMinutes, "WORKSHOP_EXCEPTION", ex.getReason());
            }
        }

        // 3. Branch-level weekly override
        DayOfWeek day = date.getDayOfWeek();
        var branchHour = operatingHourRepository.findByBranchIdAndDayOfWeek(branchId, day);
        if (branchHour.isPresent()) {
            var h = branchHour.get();
            return new EffectiveSchedule(h.isOpen(), h.getOpenTime(), h.getCloseTime(), h.getSlotDurationMinutes(), "BRANCH", null);
        }

        // 4. Workshop-level global weekly schedule
        if (workshopId != null) {
            var workshopHour = operatingHourRepository.findByWorkshopIdAndBranchIdIsNullAndDayOfWeek(workshopId, day);
            if (workshopHour.isPresent()) {
                var h = workshopHour.get();
                return new EffectiveSchedule(h.isOpen(), h.getOpenTime(), h.getCloseTime(), h.getSlotDurationMinutes(), "WORKSHOP", null);
            }
        }

        // 5. Default fallback for unconfigured workshops
        return new EffectiveSchedule(true, DEFAULT_OPEN, DEFAULT_CLOSE, DEFAULT_SLOT_MINUTES, "DEFAULT", null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeeklyHourDto> getWorkshopWeeklySchedule(UUID workshopId) {
        ensureWorkshopExists(workshopId);
        Map<DayOfWeek, OperatingHourPersistenceEntity> byDay = operatingHourRepository
                .findAllByWorkshopIdAndBranchIdIsNull(workshopId).stream()
                .collect(Collectors.toMap(OperatingHourPersistenceEntity::getDayOfWeek, Function.identity(), (a, b) -> a));

        List<WeeklyHourDto> result = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            var entity = byDay.get(day);
            if (entity != null) {
                result.add(new WeeklyHourDto(day, entity.isOpen(), entity.getOpenTime(), entity.getCloseTime(), entity.getSlotDurationMinutes(), "WORKSHOP"));
            } else {
                result.add(new WeeklyHourDto(day, true, DEFAULT_OPEN, DEFAULT_CLOSE, DEFAULT_SLOT_MINUTES, "DEFAULT"));
            }
        }
        return result;
    }

    @Override
    @Transactional
    public List<WeeklyHourDto> upsertWorkshopWeeklySchedule(UUID workshopId, List<WeeklyHourInput> items) {
        ensureWorkshopExists(workshopId);
        validateWeeklyItems(items);
        for (WeeklyHourInput item : items) {
            var entity = operatingHourRepository
                    .findByWorkshopIdAndBranchIdIsNullAndDayOfWeek(workshopId, item.dayOfWeek())
                    .orElseGet(OperatingHourPersistenceEntity::new);
            entity.setWorkshopId(workshopId);
            entity.setBranchId(null);
            applyWeeklyFields(entity, item);
            operatingHourRepository.save(entity);
        }
        return getWorkshopWeeklySchedule(workshopId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeeklyHourDto> getBranchEffectiveWeeklySchedule(UUID branchId) {
        var branch = branchRepository.findById(new BranchId(branchId))
                .orElseThrow(() -> new IllegalArgumentException("core.error.branch.notFound"));
        UUID workshopId = branch.getWorkshopId() != null ? branch.getWorkshopId().value() : null;

        Map<DayOfWeek, OperatingHourPersistenceEntity> branchByDay = operatingHourRepository
                .findAllByBranchId(branchId).stream()
                .collect(Collectors.toMap(OperatingHourPersistenceEntity::getDayOfWeek, Function.identity(), (a, b) -> a));

        Map<DayOfWeek, OperatingHourPersistenceEntity> workshopByDay = workshopId == null ? Map.of() :
                operatingHourRepository.findAllByWorkshopIdAndBranchIdIsNull(workshopId).stream()
                        .collect(Collectors.toMap(OperatingHourPersistenceEntity::getDayOfWeek, Function.identity(), (a, b) -> a));

        List<WeeklyHourDto> result = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            var branchEntity = branchByDay.get(day);
            if (branchEntity != null) {
                result.add(new WeeklyHourDto(day, branchEntity.isOpen(), branchEntity.getOpenTime(), branchEntity.getCloseTime(), branchEntity.getSlotDurationMinutes(), "BRANCH"));
                continue;
            }
            var workshopEntity = workshopByDay.get(day);
            if (workshopEntity != null) {
                result.add(new WeeklyHourDto(day, workshopEntity.isOpen(), workshopEntity.getOpenTime(), workshopEntity.getCloseTime(), workshopEntity.getSlotDurationMinutes(), "WORKSHOP"));
                continue;
            }
            result.add(new WeeklyHourDto(day, true, DEFAULT_OPEN, DEFAULT_CLOSE, DEFAULT_SLOT_MINUTES, "DEFAULT"));
        }
        return result;
    }

    @Override
    @Transactional
    public List<WeeklyHourDto> upsertBranchWeeklySchedule(UUID branchId, List<WeeklyHourInput> items) {
        var branch = branchRepository.findById(new BranchId(branchId))
                .orElseThrow(() -> new IllegalArgumentException("core.error.branch.notFound"));
        validateWeeklyItems(items);
        UUID workshopId = branch.getWorkshopId() != null ? branch.getWorkshopId().value() : null;
        for (WeeklyHourInput item : items) {
            var entity = operatingHourRepository
                    .findByBranchIdAndDayOfWeek(branchId, item.dayOfWeek())
                    .orElseGet(OperatingHourPersistenceEntity::new);
            entity.setWorkshopId(workshopId);
            entity.setBranchId(branchId);
            applyWeeklyFields(entity, item);
            operatingHourRepository.save(entity);
        }
        return getBranchEffectiveWeeklySchedule(branchId);
    }

    @Override
    @Transactional
    public void resetBranchWeeklySchedule(UUID branchId) {
        branchRepository.findById(new BranchId(branchId))
                .orElseThrow(() -> new IllegalArgumentException("core.error.branch.notFound"));
        operatingHourRepository.deleteAllByBranchId(branchId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExceptionDto> getWorkshopExceptions(UUID workshopId) {
        ensureWorkshopExists(workshopId);
        return exceptionRepository.findAllByWorkshopIdAndBranchIdIsNullOrderByExceptionDateAsc(workshopId).stream()
                .map(this::toExceptionDto)
                .toList();
    }

    @Override
    @Transactional
    public ExceptionDto upsertWorkshopException(UUID workshopId, ExceptionInput input) {
        ensureWorkshopExists(workshopId);
        validateExceptionInput(input);
        var entity = exceptionRepository
                .findByWorkshopIdAndBranchIdIsNullAndExceptionDate(workshopId, input.date())
                .orElseGet(OperatingHourExceptionPersistenceEntity::new);
        entity.setWorkshopId(workshopId);
        entity.setBranchId(null);
        applyExceptionFields(entity, input);
        return toExceptionDto(exceptionRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteWorkshopException(UUID workshopId, UUID exceptionId) {
        ensureWorkshopExists(workshopId);
        var entity = exceptionRepository.findById(exceptionId)
                .filter(e -> workshopId.equals(e.getWorkshopId()) && e.getBranchId() == null)
                .orElseThrow(() -> new IllegalArgumentException("core.error.operatingHours.exceptionNotFound"));
        exceptionRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExceptionDto> getBranchExceptions(UUID branchId) {
        branchRepository.findById(new BranchId(branchId))
                .orElseThrow(() -> new IllegalArgumentException("core.error.branch.notFound"));
        return exceptionRepository.findAllByBranchIdOrderByExceptionDateAsc(branchId).stream()
                .map(this::toExceptionDto)
                .toList();
    }

    @Override
    @Transactional
    public ExceptionDto upsertBranchException(UUID branchId, ExceptionInput input) {
        var branch = branchRepository.findById(new BranchId(branchId))
                .orElseThrow(() -> new IllegalArgumentException("core.error.branch.notFound"));
        validateExceptionInput(input);
        var entity = exceptionRepository
                .findByBranchIdAndExceptionDate(branchId, input.date())
                .orElseGet(OperatingHourExceptionPersistenceEntity::new);
        entity.setWorkshopId(branch.getWorkshopId() != null ? branch.getWorkshopId().value() : null);
        entity.setBranchId(branchId);
        applyExceptionFields(entity, input);
        return toExceptionDto(exceptionRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteBranchException(UUID branchId, UUID exceptionId) {
        branchRepository.findById(new BranchId(branchId))
                .orElseThrow(() -> new IllegalArgumentException("core.error.branch.notFound"));
        var entity = exceptionRepository.findById(exceptionId)
                .filter(e -> branchId.equals(e.getBranchId()))
                .orElseThrow(() -> new IllegalArgumentException("core.error.operatingHours.exceptionNotFound"));
        exceptionRepository.delete(entity);
    }

    private void ensureWorkshopExists(UUID workshopId) {
        workshopRepository.findById(new WorkshopId(workshopId))
                .orElseThrow(() -> new IllegalArgumentException("core.error.workshop.notFound"));
    }

    private int resolveSlotDuration(UUID branchId, UUID workshopId, DayOfWeek day) {
        return operatingHourRepository.findByBranchIdAndDayOfWeek(branchId, day)
                .map(OperatingHourPersistenceEntity::getSlotDurationMinutes)
                .or(() -> workshopId == null ? Optional.empty() :
                        operatingHourRepository.findByWorkshopIdAndBranchIdIsNullAndDayOfWeek(workshopId, day)
                                .map(OperatingHourPersistenceEntity::getSlotDurationMinutes))
                .orElse(DEFAULT_SLOT_MINUTES);
    }

    private void validateWeeklyItems(List<WeeklyHourInput> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("core.error.operatingHours.itemsRequired");
        }
        for (WeeklyHourInput item : items) {
            if (item.dayOfWeek() == null) {
                throw new IllegalArgumentException("core.error.operatingHours.dayRequired");
            }
            validateTimeWindow(item.open(), item.openTime(), item.closeTime());
            if (item.slotDurationMinutes() != null && (item.slotDurationMinutes() < 15 || item.slotDurationMinutes() > 480)) {
                throw new IllegalArgumentException("core.error.operatingHours.invalidSlotDuration");
            }
        }
    }

    private void validateExceptionInput(ExceptionInput input) {
        if (input == null || input.date() == null) {
            throw new IllegalArgumentException("core.error.operatingHours.dateRequired");
        }
        validateTimeWindow(input.open(), input.openTime(), input.closeTime());
    }

    private void validateTimeWindow(boolean open, LocalTime openTime, LocalTime closeTime) {
        if (open) {
            if (openTime == null || closeTime == null || !openTime.isBefore(closeTime)) {
                throw new IllegalArgumentException("core.error.operatingHours.invalidTimeRange");
            }
        }
    }

    private void applyWeeklyFields(OperatingHourPersistenceEntity entity, WeeklyHourInput item) {
        entity.setDayOfWeek(item.dayOfWeek());
        entity.setOpen(item.open());
        entity.setOpenTime(item.open() ? item.openTime() : null);
        entity.setCloseTime(item.open() ? item.closeTime() : null);
        entity.setSlotDurationMinutes(item.slotDurationMinutes() != null ? item.slotDurationMinutes() : DEFAULT_SLOT_MINUTES);
    }

    private void applyExceptionFields(OperatingHourExceptionPersistenceEntity entity, ExceptionInput input) {
        entity.setExceptionDate(input.date());
        entity.setOpen(input.open());
        entity.setOpenTime(input.open() ? input.openTime() : null);
        entity.setCloseTime(input.open() ? input.closeTime() : null);
        entity.setReason(input.reason());
    }

    private ExceptionDto toExceptionDto(OperatingHourExceptionPersistenceEntity e) {
        return new ExceptionDto(e.getId(), e.getWorkshopId(), e.getBranchId(), e.getExceptionDate(), e.isOpen(), e.getOpenTime(), e.getCloseTime(), e.getReason());
    }
}
