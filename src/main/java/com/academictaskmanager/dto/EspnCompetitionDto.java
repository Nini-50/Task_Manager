package com.academictaskmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** A single competition (the home-vs-away matchup) within an ESPN scoreboard event. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EspnCompetitionDto {
    private List<EspnCompetitorDto> competitors;

    public List<EspnCompetitorDto> getCompetitors() { return competitors; }
    public void setCompetitors(List<EspnCompetitorDto> competitors) { this.competitors = competitors; }
}
