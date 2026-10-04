package com.academictaskmanager.controller;

import com.academictaskmanager.model.Course;
import com.academictaskmanager.model.User;
import com.academictaskmanager.service.CourseService;
import com.academictaskmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;
    private final UserService userService;

    public CourseController(CourseService courseService, UserService userService) {
        this.courseService = courseService;
        this.userService = userService;
    }

    private User currentUser(Authentication authentication) {
        return userService.findByUsername(authentication.getName());
    }

    @GetMapping
    public List<Course> all(Authentication authentication) {
        return courseService.findAll(currentUser(authentication));
    }

    @PostMapping
    public ResponseEntity<Course> create(Authentication authentication, @Valid @RequestBody Course course) {
        course.setId(null);
        return ResponseEntity.ok(courseService.save(course, currentUser(authentication)));
    }

    @PutMapping("/{id}")
    public Course update(Authentication authentication, @PathVariable Long id, @Valid @RequestBody Course course) {
        course.setId(id);
        return courseService.save(course, currentUser(authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        courseService.delete(id, currentUser(authentication));
        return ResponseEntity.noContent().build();
    }
}
