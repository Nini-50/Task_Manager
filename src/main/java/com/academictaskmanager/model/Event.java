package com.academictaskmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * A scheduled, time-based calendar entry such as a study group, office
 * hours, or a club meeting — distinct from an {@link AcademicTask}, which
 * represents work to complete rather than an appointment with a specific
 * start (and optional end) time.
 */
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @Column(length = 2000)
    private String description;

    @NotNull
    private LocalDateTime startDateTime;

    /** Optional; when absent, the event is shown as a single point in time rather than a span. */
    private LocalDateTime endDateTime;

    private String location;

    /** Optional link to the class this event relates to (e.g. a study session for a course). */
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    /** The account this event belongs to. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    /**
     * Shared by every occurrence generated from the same recurring series (null for a one-off
     * event). Each occurrence is its own independent row, so editing or deleting one does not
     * affect the others; deleting the whole series removes every row sharing this id.
     */
    private String seriesId;

    /** Human-readable description of the recurrence rule (e.g. "Repeats weekly on Mon, Wed until Dec 15, 2026"), copied onto every occurrence for display. Null for a one-off event. */
    private String recurrenceSummary;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getStartDateTime() { return startDateTime; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }

    public LocalDateTime getEndDateTime() { return endDateTime; }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public String getSeriesId() { return seriesId; }
    public void setSeriesId(String seriesId) { this.seriesId = seriesId; }

    public String getRecurrenceSummary() { return recurrenceSummary; }
    public void setRecurrenceSummary(String recurrenceSummary) { this.recurrenceSummary = recurrenceSummary; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    public void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
