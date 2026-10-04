package com.academictaskmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Response envelope from ESPN's public (unofficial, no API key) scoreboard endpoint. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EspnScoreboardDto {
    private List<EspnEventDto> events;

    public List<EspnEventDto> getEvents() { return events; }
    public void setEvents(List<EspnEventDto> events) { this.events = events; }
}
