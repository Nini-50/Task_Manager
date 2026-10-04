package com.academictaskmanager.controller;

import com.academictaskmanager.model.Course;
import com.academictaskmanager.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<Course> all() {
        return courseService.findAll();
    }

    @PostMapping
    public ResponseEntity<Course> create(@Valid @RequestBody Course course) {
        course.setId(null);
        return ResponseEntity.ok(courseService.save(course));
    }

    @PutMapping("/{id}")
    public Course update(@PathVariable Long id, @Valid @RequestBody Course course) {
        course.setId(id);
        return courseService.save(course);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
