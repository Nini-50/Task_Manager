package com.academictaskmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Team info nested inside an ESPN scoreboard competitor, or a team-list/schedule entry. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EspnTeamDto {
    private String id;
    private String displayName;
    private String shortDisplayName;
    private String abbreviation;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getShortDisplayName() { return shortDisplayName; }
    public void setShortDisplayName(String shortDisplayName) { this.shortDisplayName = shortDisplayName; }

    public String getAbbreviation() { return abbreviation; }
    public void setAbbreviation(String abbreviation) { this.abbreviation = abbreviation; }
}
