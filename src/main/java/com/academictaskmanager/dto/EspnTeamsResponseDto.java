package com.academictaskmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Response envelope from ESPN's public team-list endpoint (.../sports/{league}/teams). */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EspnTeamsResponseDto {
    private List<Sport> sports;

    public List<Sport> getSports() { return sports; }
    public void setSports(List<Sport> sports) { this.sports = sports; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sport {
        private List<League> leagues;

        public List<League> getLeagues() { return leagues; }
        public void setLeagues(List<League> leagues) { this.leagues = leagues; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class League {
        private List<Entry> teams;

        public List<Entry> getTeams() { return teams; }
        public void setTeams(List<Entry> teams) { this.teams = teams; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entry {
        private EspnTeamDto team;

        public EspnTeamDto getTeam() { return team; }
        public void setTeam(EspnTeamDto team) { this.team = team; }
    }
}
