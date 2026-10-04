package com.academictaskmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** The `status` object on an ESPN scoreboard event. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EspnStatusDto {
    private EspnStatusTypeDto type;

    public EspnStatusTypeDto getType() { return type; }
    public void setType(EspnStatusTypeDto type) { this.type = type; }
}
