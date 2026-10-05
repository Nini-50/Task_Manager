package com.academictaskmanager.controller;

import com.academictaskmanager.dto.RecurringEventRequest;
import com.academictaskmanager.model.Event;
import com.academictaskmanager.model.User;
import com.academictaskmanager.service.EventService;
import com.academictaskmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/** REST API backing the dashboard calendar's time-based events (as opposed to tasks). */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final UserService userService;

    public EventController(EventService eventService, UserService userService) {
        this.eventService = eventService;
        this.userService = userService;
    }

    private User currentUser(Authentication authentication) {
        return userService.findByUsername(authentication.getName());
    }

    @GetMapping
    public List<Event> all(Authentication authentication) {
        return eventService.findAll(currentUser(authentication));
    }

    @GetMapping("/upcoming")
    public List<Event> upcoming(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return eventService.findUpcoming(currentUser(authentication), start, end);
    }

    @GetMapping("/{id}")
    public Event one(Authentication authentication, @PathVariable Long id) {
        return eventService.findById(id, currentUser(authentication));
    }

    @PostMapping
    public ResponseEntity<Event> create(Authentication authentication, @Valid @RequestBody Event event) {
        event.setId(null);
        return ResponseEntity.ok(eventService.save(event, currentUser(authentication)));
    }

    /** Creates a recurring series of events; returns every materialized occurrence. */
    @PostMapping("/series")
    public ResponseEntity<List<Event>> createSeries(Authentication authentication, @Valid @RequestBody RecurringEventRequest request) {
        request.getEvent().setId(null);
        return ResponseEntity.ok(eventService.createRecurringSeries(request.getEvent(), request.getRecurrence(), currentUser(authentication)));
    }

    /** Deletes every occurrence of a recurring series. */
    @DeleteMapping("/series/{seriesId}")
    public ResponseEntity<Void> deleteSeries(Authentication authentication, @PathVariable String seriesId) {
        eventService.deleteSeries(seriesId, currentUser(authentication));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public Event update(Authentication authentication, @PathVariable Long id, @Valid @RequestBody Event event) {
        User owner = currentUser(authentication);
        // Editing a single occurrence shouldn't sever its series membership or wipe the
        // human-readable summary; the edit form never submits those fields, so carry them over.
        Event existing = eventService.findById(id, owner);
        event.setId(id);
        event.setSeriesId(existing.getSeriesId());
        event.setRecurrenceSummary(existing.getRecurrenceSummary());
        return eventService.save(event, owner);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        eventService.delete(id, currentUser(authentication));
        return ResponseEntity.noContent().build();
    }
}
