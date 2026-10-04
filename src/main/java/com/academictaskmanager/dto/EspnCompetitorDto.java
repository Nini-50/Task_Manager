package com.academictaskmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** A single competitor (home or away) within an ESPN scoreboard competition. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EspnCompetitorDto {
    private String homeAway;
    private String score;
    private EspnTeamDto team;

    public String getHomeAway() { return homeAway; }
    public void setHomeAway(String homeAway) { this.homeAway = homeAway; }

    public String getScore() { return score; }
    public void setScore(String score) { this.score = score; }

    public EspnTeamDto getTeam() { return team; }
    public void setTeam(EspnTeamDto team) { this.team = team; }
}
