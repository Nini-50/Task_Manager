package com.academictaskmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A course the student is enrolled in.
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

    private String instructor;

    /** The account this course belongs to. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Ignored by Jackson: serializing this back-reference alongside AcademicTask.course would
    // either recurse infinitely or lazily hit a closed Hibernate session outside a transaction.
    // Clients fetch a course's tasks via GET /api/tasks?courseId=... instead.
    @JsonIgnore
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

    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<AcademicTask> getTasks() { return tasks; }
    public void setTasks(List<AcademicTask> tasks) { this.tasks = tasks; }
}
