package com.academictaskmanager.repository;

import com.academictaskmanager.model.AcademicTask;
import com.academictaskmanager.model.TaskStatus;
import com.academictaskmanager.model.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AcademicTaskRepository extends JpaRepository<AcademicTask, Long> {

    List<AcademicTask> findByDueDateBetweenOrderByDueDateAsc(LocalDateTime start, LocalDateTime end);

    List<AcademicTask> findByTypeOrderByDueDateAsc(TaskType type);

    List<AcademicTask> findByStatusNotOrderByDueDateAsc(TaskStatus status);

    List<AcademicTask> findAllByOrderByDueDateAsc();

    Optional<AcademicTask> findByCanvasAssignmentId(Long canvasAssignmentId);
}
