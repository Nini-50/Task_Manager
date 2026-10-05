package com.academictaskmanager.dto;

import com.academictaskmanager.model.RecurrenceFrequency;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Client-supplied recurrence rule used when creating a recurring series of tasks or events.
 * Each occurrence is materialized as its own independent row (see {@code seriesId} on
 * {@link com.academictaskmanager.model.Event}/{@link com.academictaskmanager.model.AcademicTask}),
 * so this request is only consulted at creation time, not stored as-is.
 */
public class RecurrenceRequest {

    @NotNull
    private RecurrenceFrequency frequency;

    /** Repeat every N days/weeks/months; defaults to 1 when null or less than 1. */
    private Integer interval;

    /** Which weekdays to repeat on; only used for WEEKLY. Defaults to the seed item's own weekday when omitted. */
    private Set<DayOfWeek> daysOfWeek;

    /** Last date an occurrence may fall on (inclusive); required. */
    @NotNull
    private LocalDate until;

    public RecurrenceFrequency getFrequency() { return frequency; }
    public void setFrequency(RecurrenceFrequency frequency) { this.frequency = frequency; }

    public Integer getInterval() { return interval; }
    public void setInterval(Integer interval) { this.interval = interval; }

    public Set<DayOfWeek> getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(Set<DayOfWeek> daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public LocalDate getUntil() { return until; }
    public void setUntil(LocalDate until) { this.until = until; }
}
