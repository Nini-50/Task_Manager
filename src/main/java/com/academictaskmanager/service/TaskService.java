package com.academictaskmanager.service;

import com.academictaskmanager.model.AcademicTask;
import com.academictaskmanager.model.TaskStatus;
import com.academictaskmanager.model.User;
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

    public List<AcademicTask> findAll(User owner) {
        return taskRepository.findByOwnerOrderByDueDateAsc(owner);
    }

    public AcademicTask findById(Long id, User owner) {
        return taskRepository.findByIdAndOwner(id, owner).orElseThrow(() ->
                new IllegalArgumentException("Task not found: " + id));
    }

    public AcademicTask save(AcademicTask task, User owner) {
        task.setOwner(owner);
        return taskRepository.save(task);
    }

    public void delete(Long id, User owner) {
        AcademicTask task = findById(id, owner);
        taskRepository.delete(task);
    }

    public AcademicTask markComplete(Long id, User owner) {
        AcademicTask task = findById(id, owner);
        task.setStatus(TaskStatus.COMPLETED);
        return taskRepository.save(task);
    }

    /** Tasks due within the given window, ordered soonest first — powers the calendar view. */
    public List<AcademicTask> findUpcoming(User owner, LocalDateTime start, LocalDateTime end) {
        return taskRepository.findByOwnerAndDueDateBetweenOrderByDueDateAsc(owner, start, end);
    }

    /** Active (non-completed) to-dos sorted by priority then due date — powers the to-do widget. */
    public List<AcademicTask> findActiveTodos(User owner) {
        return taskRepository.findByOwnerAndStatusNotOrderByDueDateAsc(owner, TaskStatus.COMPLETED).stream()
                .sorted(Comparator.comparingInt(AcademicTask::getPriority).reversed()
                        .thenComparing(AcademicTask::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }
}
