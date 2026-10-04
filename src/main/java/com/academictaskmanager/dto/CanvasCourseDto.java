package com.academictaskmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Maps the subset of fields we need from Canvas LMS's `GET /api/v1/courses` response. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CanvasCourseDto {
    private Long id;
    private String name;
    private String course_code;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCourseCode() { return course_code; }
    public void setCourseCode(String courseCode) { this.course_code = courseCode; }
}
