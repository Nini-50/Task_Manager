package com.academictaskmanager.repository;

import com.academictaskmanager.model.AcademicTask;
import com.academictaskmanager.model.TaskStatus;
import com.academictaskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AcademicTaskRepository extends JpaRepository<AcademicTask, Long> {

    List<AcademicTask> findByOwnerAndDueDateBetweenOrderByDueDateAsc(User owner, LocalDateTime start, LocalDateTime end);

    List<AcademicTask> findByOwnerAndStatusNotOrderByDueDateAsc(User owner, TaskStatus status);

    List<AcademicTask> findByOwnerOrderByDueDateAsc(User owner);

    Optional<AcademicTask> findByIdAndOwner(Long id, User owner);
}
