package com.academictaskmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** One game on an ESPN scoreboard. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EspnEventDto {
    private String name;
    private String shortName;
    private EspnStatusDto status;
    private List<EspnCompetitionDto> competitions;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }

    public EspnStatusDto getStatus() { return status; }
    public void setStatus(EspnStatusDto status) { this.status = status; }

    public List<EspnCompetitionDto> getCompetitions() { return competitions; }
    public void setCompetitions(List<EspnCompetitionDto> competitions) { this.competitions = competitions; }
}
