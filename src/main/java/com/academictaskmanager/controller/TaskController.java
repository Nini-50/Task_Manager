package com.academictaskmanager.controller;

import com.academictaskmanager.dto.RecurringTaskRequest;
import com.academictaskmanager.model.AcademicTask;
import com.academictaskmanager.model.User;
import com.academictaskmanager.service.TaskService;
import com.academictaskmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/** REST API backing the dashboard's calendar and to-do widgets. */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;

    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    private User currentUser(Authentication authentication) {
        return userService.findByUsername(authentication.getName());
    }

    @GetMapping
    public List<AcademicTask> all(Authentication authentication) {
        return taskService.findAll(currentUser(authentication));
    }

    @GetMapping("/upcoming")
    public List<AcademicTask> upcoming(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return taskService.findUpcoming(currentUser(authentication), start, end);
    }

    @GetMapping("/todos")
    public List<AcademicTask> todos(Authentication authentication) {
        return taskService.findActiveTodos(currentUser(authentication));
    }

    @GetMapping("/{id}")
    public AcademicTask one(Authentication authentication, @PathVariable Long id) {
        return taskService.findById(id, currentUser(authentication));
    }

    @PostMapping
    public ResponseEntity<AcademicTask> create(Authentication authentication, @Valid @RequestBody AcademicTask task) {
        task.setId(null);
        return ResponseEntity.ok(taskService.save(task, currentUser(authentication)));
    }

    /** Creates a recurring series of tasks; returns every materialized occurrence. */
    @PostMapping("/series")
    public ResponseEntity<List<AcademicTask>> createSeries(Authentication authentication, @Valid @RequestBody RecurringTaskRequest request) {
        request.getTask().setId(null);
        return ResponseEntity.ok(taskService.createRecurringSeries(request.getTask(), request.getRecurrence(), currentUser(authentication)));
    }

    /** Deletes every occurrence of a recurring series. */
    @DeleteMapping("/series/{seriesId}")
    public ResponseEntity<Void> deleteSeries(Authentication authentication, @PathVariable String seriesId) {
        taskService.deleteSeries(seriesId, currentUser(authentication));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public AcademicTask update(Authentication authentication, @PathVariable Long id, @Valid @RequestBody AcademicTask task) {
        User owner = currentUser(authentication);
        // Editing a single occurrence shouldn't sever its series membership or wipe the
        // human-readable summary; the edit form never submits those fields, so carry them over.
        AcademicTask existing = taskService.findById(id, owner);
        task.setId(id);
        task.setSeriesId(existing.getSeriesId());
        task.setRecurrenceSummary(existing.getRecurrenceSummary());
        return taskService.save(task, owner);
    }

    @PostMapping("/{id}/complete")
    public AcademicTask complete(Authentication authentication, @PathVariable Long id) {
        return taskService.markComplete(id, currentUser(authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        taskService.delete(id, currentUser(authentication));
        return ResponseEntity.noContent().build();
    }
}
