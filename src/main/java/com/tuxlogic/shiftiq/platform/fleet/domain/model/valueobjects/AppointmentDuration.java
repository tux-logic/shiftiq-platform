package com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects;

import java.time.LocalDateTime;

/**
 * Value Object representing the duration of an appointment in hours.
 *
 * <p>Use {@link #defaultDuration()} to obtain the standard 1-hour duration.
 * Compute the end time with {@link #calculateEnd(LocalDateTime)}.</p>
 */
public record AppointmentDuration(int hours) {

    public AppointmentDuration {
        if (hours <= 0) {
            throw new IllegalArgumentException("Appointment duration must be greater than zero");
        }
    }

    /** Returns the standard default appointment duration (1 hour). */
    public static AppointmentDuration defaultDuration() {
        return new AppointmentDuration(1);
    }

    /**
     * Calculates the scheduled end time given a start time.
     *
     * @param start the appointment start time
     * @return start time plus this duration in hours
     */
    public LocalDateTime calculateEnd(LocalDateTime start) {
        if (start == null) {
            throw new IllegalArgumentException("Scheduled start time is required");
        }
        return start.plusHours(hours);
    }
}
