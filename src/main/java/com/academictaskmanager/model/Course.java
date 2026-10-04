package com.academictaskmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A course the student is enrolled in. Can originate from a manual entry or be
 * synced from Canvas LMS (in which case canvasCourseId is populated).
 */
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    /** Short course code, e.g. "CS 101". */
    private String code;

    /** Hex color used to color-code this course across the dashboard/calendar. */
    private String colorHex = "#4f46e5";

    /** Non-null when this course was imported from Canvas. */
    private Long canvasCourseId;

    private String instructor;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AcademicTask> tasks = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    public Long getCanvasCourseId() { return canvasCourseId; }
    public void setCanvasCourseId(Long canvasCourseId) { this.canvasCourseId = canvasCourseId; }

    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<AcademicTask> getTasks() { return tasks; }
    public void setTasks(List<AcademicTask> tasks) { this.tasks = tasks; }
}
