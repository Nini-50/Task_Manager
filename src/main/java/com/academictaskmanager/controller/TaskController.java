package com.academictaskmanager.controller;

import com.academictaskmanager.model.AcademicTask;
import com.academictaskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/** REST API backing the dashboard's calendar and to-do widgets. */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<AcademicTask> all() {
        return taskService.findAll();
    }

    @GetMapping("/upcoming")
    public List<AcademicTask> upcoming(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return taskService.findUpcoming(start, end);
    }

    @GetMapping("/todos")
    public List<AcademicTask> todos() {
        return taskService.findActiveTodos();
    }

    @GetMapping("/{id}")
    public AcademicTask one(@PathVariable Long id) {
        return taskService.findById(id);
    }

    @PostMapping
    public ResponseEntity<AcademicTask> create(@Valid @RequestBody AcademicTask task) {
        task.setId(null);
        return ResponseEntity.ok(taskService.save(task));
    }

    @PutMapping("/{id}")
    public AcademicTask update(@PathVariable Long id, @Valid @RequestBody AcademicTask task) {
        task.setId(id);
        return taskService.save(task);
    }

    @PostMapping("/{id}/complete")
    public AcademicTask complete(@PathVariable Long id) {
        return taskService.markComplete(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
