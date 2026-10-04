package com.academictaskmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Response envelope from ESPN's public team-schedule endpoint (.../teams/{id}/schedule). */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EspnScheduleResponseDto {
    private List<EspnEventDto> events;

    public List<EspnEventDto> getEvents() { return events; }
    public void setEvents(List<EspnEventDto> events) { this.events = events; }
}
