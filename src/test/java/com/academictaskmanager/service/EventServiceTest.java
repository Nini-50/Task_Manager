package com.academictaskmanager.service;

import com.academictaskmanager.model.Event;
import com.academictaskmanager.model.User;
import com.academictaskmanager.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    private EventService eventService;
    private User owner;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventRepository);
        owner = new User();
        owner.setId(1L);
        owner.setUsername("alice");
    }

    @Test
    void findUpcomingDelegatesToRepository() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 1, 0, 0);
        Event event = new Event();
        event.setTitle("Study group");
        when(eventRepository.findByOwnerAndStartDateTimeBetweenOrderByStartDateTimeAsc(owner, start, end))
                .thenReturn(List.of(event));

        assertThat(eventService.findUpcoming(owner, start, end)).containsExactly(event);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(eventRepository.findByIdAndOwner(5L, owner)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.findById(5L, owner))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5");
    }

    @Test
    void saveSetsOwner() {
        Event event = new Event();
        event.setTitle("Office hours");
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        Event saved = eventService.save(event, owner);

        assertThat(saved.getOwner()).isEqualTo(owner);
    }

    @Test
    void deleteDelegatesToRepository() {
        Event event = new Event();
        event.setId(3L);
        when(eventRepository.findByIdAndOwner(3L, owner)).thenReturn(Optional.of(event));

        eventService.delete(3L, owner);

        verify(eventRepository).delete(event);
    }

    @Test
    void createRecurringSeriesMaterializesOneRowPerOccurrence() {
        Event seed = new Event();
        seed.setTitle("Study group");
        seed.setStartDateTime(LocalDateTime.of(2026, 3, 2, 9, 0));
        seed.setEndDateTime(LocalDateTime.of(2026, 3, 2, 10, 0));
        com.academictaskmanager.dto.RecurrenceRequest recurrence = new com.academictaskmanager.dto.RecurrenceRequest();
        recurrence.setFrequency(com.academictaskmanager.model.RecurrenceFrequency.WEEKLY);
        recurrence.setUntil(java.time.LocalDate.of(2026, 3, 16));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Event> saved = eventService.createRecurringSeries(seed, recurrence, owner);

        assertThat(saved).hasSize(3);
        assertThat(saved).extracting(Event::getSeriesId).doesNotContainNull();
        assertThat(saved.stream().map(Event::getSeriesId).distinct()).hasSize(1);
        assertThat(saved).allMatch(e -> e.getOwner() == owner);
        assertThat(saved.get(0).getEndDateTime()).isEqualTo(LocalDateTime.of(2026, 3, 2, 10, 0));
        assertThat(saved.get(1).getStartDateTime()).isEqualTo(LocalDateTime.of(2026, 3, 9, 9, 0));
    }

    @Test
    void deleteSeriesDeletesEveryMatchingRow() {
        Event a = new Event();
        a.setId(1L);
        Event b = new Event();
        b.setId(2L);
        when(eventRepository.findBySeriesIdAndOwner("series-1", owner)).thenReturn(List.of(a, b));

        eventService.deleteSeries("series-1", owner);

        verify(eventRepository).delete(a);
        verify(eventRepository).delete(b);
    }
}
