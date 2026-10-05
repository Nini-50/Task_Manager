package com.academictaskmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * A single note "tab" that belongs to a course. A course can have any number
 * of notes (e.g. "Lecture 1", "Midterm review"); each one is its own small
 * document with a title and free-form text content.
 */
@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @Column(length = 10000)
    private String content;

    /** Ordering among a course's notes, so note tabs appear in a stable, user-controlled order. */
    private int position = 0;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    /** The account this note belongs to. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    public void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
