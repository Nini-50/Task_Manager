package com.academictaskmanager.dto;

import com.academictaskmanager.model.Event;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/** Request body for creating a recurring series of events: the template event plus the rule. */
public class RecurringEventRequest {

    @NotNull
    @Valid
    private Event event;

    @NotNull
    @Valid
    private RecurrenceRequest recurrence;

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public RecurrenceRequest getRecurrence() { return recurrence; }
    public void setRecurrence(RecurrenceRequest recurrence) { this.recurrence = recurrence; }
}
