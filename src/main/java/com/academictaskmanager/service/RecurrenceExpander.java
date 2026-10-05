package com.academictaskmanager.service;

import com.academictaskmanager.dto.RecurrenceRequest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Pure-function helper that expands a {@link RecurrenceRequest} into the concrete list of
 * occurrence start date-times, preserving the seed's time-of-day. Each returned date-time is
 * later materialized as its own independent row by the caller (see
 * {@code EventService}/{@code TaskService}), so this class has no persistence concerns.
 */
public final class RecurrenceExpander {

    /** Hard cap on generated occurrences so a careless "daily, until 2099" request can't blow up the database. */
    public static final int MAX_OCCURRENCES = 200;

    private RecurrenceExpander() {
    }

    public static List<LocalDateTime> expand(LocalDateTime seedStart, RecurrenceRequest recurrence) {
        if (seedStart == null) {
            throw new IllegalArgumentException("Seed start date/time is required");
        }
        if (recurrence == null || recurrence.getFrequency() == null) {
            throw new IllegalArgumentException("Recurrence frequency is required");
        }
        LocalDate until = recurrence.getUntil();
        if (until == null) {
            throw new IllegalArgumentException("Recurrence 'until' date is required");
        }
        if (until.isBefore(seedStart.toLocalDate())) {
            throw new IllegalArgumentException("Recurrence 'until' date must be on or after the start date");
        }
        int interval = (recurrence.getInterval() != null && recurrence.getInterval() >= 1) ? recurrence.getInterval() : 1;

        List<LocalDateTime> result = new ArrayList<>();
        switch (recurrence.getFrequency()) {
            case DAILY -> expandDaily(seedStart, until, interval, result);
            case WEEKLY -> expandWeekly(seedStart, until, interval, recurrence.getDaysOfWeek(), result);
            case MONTHLY -> expandMonthly(seedStart, until, interval, result);
        }
        if (result.size() > MAX_OCCURRENCES) {
            return result.subList(0, MAX_OCCURRENCES);
        }
        return result;
    }

    /** Builds a human-readable description like "Repeats weekly on Mon, Wed, Fri until Dec 15, 2026". */
    public static String summarize(RecurrenceRequest recurrence) {
        int interval = (recurrence.getInterval() != null && recurrence.getInterval() >= 1) ? recurrence.getInterval() : 1;
        String untilText = recurrence.getUntil().format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"));
        String frequencyText = switch (recurrence.getFrequency()) {
            case DAILY -> interval == 1 ? "daily" : "every " + interval + " days";
            case WEEKLY -> {
                String base = interval == 1 ? "weekly" : "every " + interval + " weeks";
                if (recurrence.getDaysOfWeek() != null && !recurrence.getDaysOfWeek().isEmpty()) {
                    String dayList = new TreeSet<>(recurrence.getDaysOfWeek()).stream()
                            .map(d -> d.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH))
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("");
                    yield base + " on " + dayList;
                }
                yield base;
            }
            case MONTHLY -> interval == 1 ? "monthly" : "every " + interval + " months";
        };
        return "Repeats " + frequencyText + " until " + untilText;
    }

    private static void expandDaily(LocalDateTime seedStart, LocalDate until, int interval, List<LocalDateTime> result) {
        LocalDateTime cursor = seedStart;
        while (!cursor.toLocalDate().isAfter(until) && result.size() < MAX_OCCURRENCES) {
            result.add(cursor);
            cursor = cursor.plusDays(interval);
        }
    }

    private static void expandMonthly(LocalDateTime seedStart, LocalDate until, int interval, List<LocalDateTime> result) {
        LocalDateTime cursor = seedStart;
        while (!cursor.toLocalDate().isAfter(until) && result.size() < MAX_OCCURRENCES) {
            result.add(cursor);
            cursor = cursor.plusMonths(interval);
        }
    }

    private static void expandWeekly(LocalDateTime seedStart, LocalDate until, int interval, Set<DayOfWeek> requestedDays, List<LocalDateTime> result) {
        Set<DayOfWeek> days = (requestedDays == null || requestedDays.isEmpty())
                ? Set.of(seedStart.getDayOfWeek())
                : requestedDays;
        List<DayOfWeek> sortedDays = new ArrayList<>(new TreeSet<>(days));

        LocalDate seedDate = seedStart.toLocalDate();
        LocalDate weekMonday = seedDate.minusDays(seedDate.getDayOfWeek().getValue() - 1);

        // A week's Monday is its earliest possible day, so once it's past `until` no day in
        // that week (or any later week) can still be in range.
        while (!weekMonday.isAfter(until) && result.size() < MAX_OCCURRENCES) {
            for (DayOfWeek day : sortedDays) {
                LocalDate occurrenceDate = weekMonday.plusDays(day.getValue() - 1);
                if (occurrenceDate.isBefore(seedDate) || occurrenceDate.isAfter(until)) {
                    continue;
                }
                result.add(occurrenceDate.atTime(seedStart.toLocalTime()));
                if (result.size() >= MAX_OCCURRENCES) {
                    return;
                }
            }
            weekMonday = weekMonday.plusWeeks(interval);
        }
    }
}
