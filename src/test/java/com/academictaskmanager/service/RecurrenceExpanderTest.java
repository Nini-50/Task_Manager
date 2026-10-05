package com.academictaskmanager.service;

import com.academictaskmanager.dto.RecurrenceRequest;
import com.academictaskmanager.model.RecurrenceFrequency;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecurrenceExpanderTest {

    private RecurrenceRequest request(RecurrenceFrequency frequency, Integer interval, Set<DayOfWeek> days, LocalDate until) {
        RecurrenceRequest r = new RecurrenceRequest();
        r.setFrequency(frequency);
        r.setInterval(interval);
        r.setDaysOfWeek(days);
        r.setUntil(until);
        return r;
    }

    @Test
    void dailyExpandsEveryDayThroughUntilInclusive() {
        LocalDateTime seed = LocalDateTime.of(2026, 3, 2, 9, 0);
        RecurrenceRequest r = request(RecurrenceFrequency.DAILY, 1, null, LocalDate.of(2026, 3, 5));

        List<LocalDateTime> result = RecurrenceExpander.expand(seed, r);

        assertThat(result).containsExactly(
                LocalDateTime.of(2026, 3, 2, 9, 0),
                LocalDateTime.of(2026, 3, 3, 9, 0),
                LocalDateTime.of(2026, 3, 4, 9, 0),
                LocalDateTime.of(2026, 3, 5, 9, 0));
    }

    @Test
    void weeklyDefaultsToSeedWeekdayWhenDaysOmitted() {
        // Monday, March 2 2026
        LocalDateTime seed = LocalDateTime.of(2026, 3, 2, 10, 30);
        RecurrenceRequest r = request(RecurrenceFrequency.WEEKLY, 1, null, LocalDate.of(2026, 3, 16));

        List<LocalDateTime> result = RecurrenceExpander.expand(seed, r);

        assertThat(result).containsExactly(
                LocalDateTime.of(2026, 3, 2, 10, 30),
                LocalDateTime.of(2026, 3, 9, 10, 30),
                LocalDateTime.of(2026, 3, 16, 10, 30));
    }

    @Test
    void weeklyHonorsMultipleSelectedDays() {
        // Monday, March 2 2026; repeat Mon/Wed/Fri through March 13 (Friday)
        LocalDateTime seed = LocalDateTime.of(2026, 3, 2, 8, 0);
        RecurrenceRequest r = request(RecurrenceFrequency.WEEKLY, 1,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), LocalDate.of(2026, 3, 13));

        List<LocalDateTime> result = RecurrenceExpander.expand(seed, r);

        assertThat(result).containsExactly(
                LocalDateTime.of(2026, 3, 2, 8, 0),
                LocalDateTime.of(2026, 3, 4, 8, 0),
                LocalDateTime.of(2026, 3, 6, 8, 0),
                LocalDateTime.of(2026, 3, 9, 8, 0),
                LocalDateTime.of(2026, 3, 11, 8, 0),
                LocalDateTime.of(2026, 3, 13, 8, 0));
    }

    @Test
    void weeklyEveryOtherWeekSkipsAlternateWeeks() {
        LocalDateTime seed = LocalDateTime.of(2026, 3, 2, 8, 0);
        RecurrenceRequest r = request(RecurrenceFrequency.WEEKLY, 2, null, LocalDate.of(2026, 3, 30));

        List<LocalDateTime> result = RecurrenceExpander.expand(seed, r);

        assertThat(result).containsExactly(
                LocalDateTime.of(2026, 3, 2, 8, 0),
                LocalDateTime.of(2026, 3, 16, 8, 0),
                LocalDateTime.of(2026, 3, 30, 8, 0));
    }

    @Test
    void monthlyExpandsOnSameDayOfMonth() {
        LocalDateTime seed = LocalDateTime.of(2026, 1, 15, 12, 0);
        RecurrenceRequest r = request(RecurrenceFrequency.MONTHLY, 1, null, LocalDate.of(2026, 4, 15));

        List<LocalDateTime> result = RecurrenceExpander.expand(seed, r);

        assertThat(result).containsExactly(
                LocalDateTime.of(2026, 1, 15, 12, 0),
                LocalDateTime.of(2026, 2, 15, 12, 0),
                LocalDateTime.of(2026, 3, 15, 12, 0),
                LocalDateTime.of(2026, 4, 15, 12, 0));
    }

    @Test
    void cappedAtMaxOccurrences() {
        LocalDateTime seed = LocalDateTime.of(2026, 1, 1, 9, 0);
        RecurrenceRequest r = request(RecurrenceFrequency.DAILY, 1, null, LocalDate.of(2030, 1, 1));

        List<LocalDateTime> result = RecurrenceExpander.expand(seed, r);

        assertThat(result).hasSize(RecurrenceExpander.MAX_OCCURRENCES);
    }

    @Test
    void untilBeforeStartThrows() {
        LocalDateTime seed = LocalDateTime.of(2026, 3, 2, 9, 0);
        RecurrenceRequest r = request(RecurrenceFrequency.DAILY, 1, null, LocalDate.of(2026, 3, 1));

        assertThatThrownBy(() -> RecurrenceExpander.expand(seed, r))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void summarizeDescribesWeeklyWithDays() {
        RecurrenceRequest r = request(RecurrenceFrequency.WEEKLY, 1,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), LocalDate.of(2026, 12, 15));

        assertThat(RecurrenceExpander.summarize(r)).isEqualTo("Repeats weekly on Mon, Wed until Dec 15, 2026");
    }
}
