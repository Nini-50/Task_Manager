package com.academictaskmanager.controller;

import com.academictaskmanager.model.AcademicTask;
import com.academictaskmanager.model.Course;
import com.academictaskmanager.model.User;
import com.academictaskmanager.service.CourseService;
import com.academictaskmanager.service.SyllabusParsingService;
import com.academictaskmanager.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Handles syllabus upload: step 1 extracts candidate dated items for the
 * student to review, step 2 saves the ones they confirm.
 */
@RestController
@RequestMapping("/api/syllabus")
public class SyllabusController {

    private final SyllabusParsingService syllabusParsingService;
    private final CourseService courseService;
    private final UserService userService;

    public SyllabusController(SyllabusParsingService syllabusParsingService, CourseService courseService,
                               UserService userService) {
        this.syllabusParsingService = syllabusParsingService;
        this.courseService = courseService;
        this.userService = userService;
    }

    @PostMapping("/preview")
    public ResponseEntity<?> preview(Authentication authentication,
                                      @RequestParam("file") MultipartFile file,
                                      @RequestParam(value = "courseId", required = false) Long courseId) {
        try {
            User owner = userService.findByUsername(authentication.getName());
            Course course = courseId != null ? courseService.findById(courseId, owner) : null;
            List<AcademicTask> candidates = syllabusParsingService.extractCandidateTasks(file, course);
            return ResponseEntity.ok(candidates);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error",
                    "message", "Could not read file: " + e.getMessage()));
        }
    }

    @PostMapping("/confirm")
    public List<AcademicTask> confirm(Authentication authentication, @RequestBody List<AcademicTask> tasks) {
        User owner = userService.findByUsername(authentication.getName());
        return syllabusParsingService.saveTasks(tasks, owner);
    }
}
