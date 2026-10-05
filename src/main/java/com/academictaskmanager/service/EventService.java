package com.academictaskmanager.service;

import com.academictaskmanager.model.Event;
import com.academictaskmanager.model.User;
import com.academictaskmanager.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /** Events starting within the given window, ordered soonest first — powers the calendar view. */
    public List<Event> findUpcoming(User owner, LocalDateTime start, LocalDateTime end) {
        return eventRepository.findByOwnerAndStartDateTimeBetweenOrderByStartDateTimeAsc(owner, start, end);
    }

    public List<Event> findAll(User owner) {
        return eventRepository.findByOwnerOrderByStartDateTimeAsc(owner);
    }

    public Event findById(Long id, User owner) {
        return eventRepository.findByIdAndOwner(id, owner).orElseThrow(() ->
                new IllegalArgumentException("Event not found: " + id));
    }

    public Event save(Event event, User owner) {
        event.setOwner(owner);
        return eventRepository.save(event);
    }

    public void delete(Long id, User owner) {
        Event event = findById(id, owner);
        eventRepository.delete(event);
    }
}
