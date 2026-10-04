package com.academictaskmanager.service;

import com.academictaskmanager.model.AcademicTask;
import com.academictaskmanager.model.TaskStatus;
import com.academictaskmanager.repository.AcademicTaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class TaskService {

    private final AcademicTaskRepository taskRepository;

    public TaskService(AcademicTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<AcademicTask> findAll() {
        return taskRepository.findAllByOrderByDueDateAsc();
    }

    public AcademicTask findById(Long id) {
        return taskRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Task not found: " + id));
    }

    public AcademicTask save(AcademicTask task) {
        return taskRepository.save(task);
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }

    public AcademicTask markComplete(Long id) {
        AcademicTask task = findById(id);
        task.setStatus(TaskStatus.COMPLETED);
        return taskRepository.save(task);
    }

    /** Tasks due within the given window, ordered soonest first — powers the calendar view. */
    public List<AcademicTask> findUpcoming(LocalDateTime start, LocalDateTime end) {
        return taskRepository.findByDueDateBetweenOrderByDueDateAsc(start, end);
    }

    /** Active (non-completed) to-dos sorted by priority then due date — powers the to-do widget. */
    public List<AcademicTask> findActiveTodos() {
        return taskRepository.findByStatusNotOrderByDueDateAsc(TaskStatus.COMPLETED).stream()
                .sorted(Comparator.comparingInt(AcademicTask::getPriority).reversed()
                        .thenComparing(AcademicTask::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }
}
