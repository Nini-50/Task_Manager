package com.academictaskmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Maps the subset of fields we need from Canvas LMS's `GET /api/v1/courses/:id/assignments` response. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CanvasAssignmentDto {
    private Long id;
    private String name;
    private String description;
    private String due_at; // ISO-8601 string, e.g. "2026-10-12T23:59:00Z"
    private boolean is_quiz_assignment;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDueAt() { return due_at; }
    public void setDueAt(String dueAt) { this.due_at = dueAt; }

    public boolean isQuizAssignment() { return is_quiz_assignment; }
    public void setQuizAssignment(boolean quizAssignment) { this.is_quiz_assignment = quizAssignment; }
}
