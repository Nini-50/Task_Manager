package com.academictaskmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** The `status.type` object on an ESPN scoreboard event (e.g. "Final", "In Progress"). */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EspnStatusTypeDto {
    private String description;
    private String detail;
    private boolean completed;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
