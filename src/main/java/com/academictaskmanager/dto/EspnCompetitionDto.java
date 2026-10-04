package com.academictaskmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** A single competition (the home-vs-away matchup) within an ESPN scoreboard or schedule event. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EspnCompetitionDto {
    private List<EspnCompetitorDto> competitors;
    /** Only present on team-schedule responses; scoreboard events carry status at the event level instead. */
    private EspnStatusDto status;

    public List<EspnCompetitorDto> getCompetitors() { return competitors; }
    public void setCompetitors(List<EspnCompetitorDto> competitors) { this.competitors = competitors; }

    public EspnStatusDto getStatus() { return status; }
    public void setStatus(EspnStatusDto status) { this.status = status; }
}
