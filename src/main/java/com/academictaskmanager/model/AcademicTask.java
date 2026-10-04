package com.academictaskmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * A single unit of academic work: a Canvas-synced assignment/quiz/exam, a
 * syllabus-derived event, or a manually created to-do item. Everything shown
 * on the dashboard calendar and to-do list is backed by this entity.
 */
@Entity
@Table(name = "academic_tasks")
public class AcademicTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private TaskType type = TaskType.TODO;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.PENDING;

    private LocalDateTime dueDate;

    /** Priority 1 (low) - 5 (urgent); used for sorting the to-do list. */
    private int priority = 3;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    /** Non-null when this task was imported from a Canvas assignment. */
    private Long canvasAssignmentId;

    /** True when this task was parsed out of an uploaded syllabus rather than Canvas or manual entry. */
    private boolean fromSyllabus = false;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskType getType() { return type; }
    public void setType(TaskType type) { this.type = type; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public Long getCanvasAssignmentId() { return canvasAssignmentId; }
    public void setCanvasAssignmentId(Long canvasAssignmentId) { this.canvasAssignmentId = canvasAssignmentId; }

    public boolean isFromSyllabus() { return fromSyllabus; }
    public void setFromSyllabus(boolean fromSyllabus) { this.fromSyllabus = fromSyllabus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    public void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
