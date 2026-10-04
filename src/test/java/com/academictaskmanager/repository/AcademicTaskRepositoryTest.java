package com.academictaskmanager.repository;

import com.academictaskmanager.model.AcademicTask;
import com.academictaskmanager.model.TaskStatus;
import com.academictaskmanager.model.TaskType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AcademicTaskRepositoryTest {

    @Autowired
    private AcademicTaskRepository taskRepository;

    @Test
    void findByDueDateBetweenOrderByDueDateAscReturnsOnlyTasksInWindow() {
        save("Due before window", TaskType.TODO, LocalDateTime.of(2026, 9, 1, 0, 0));
        AcademicTask inWindowEarly = save("In window, earlier", TaskType.ASSIGNMENT, LocalDateTime.of(2026, 10, 5, 0, 0));
        AcademicTask inWindowLater = save("In window, later", TaskType.QUIZ, LocalDateTime.of(2026, 10, 10, 0, 0));
        save("Due after window", TaskType.EXAM, LocalDateTime.of(2026, 11, 1, 0, 0));

        List<AcademicTask> results = taskRepository.findByDueDateBetweenOrderByDueDateAsc(
                LocalDateTime.of(2026, 10, 1, 0, 0), LocalDateTime.of(2026, 10, 31, 0, 0));

        assertThat(results).containsExactly(inWindowEarly, inWindowLater);
    }

    @Test
    void findByStatusNotOrderByDueDateAscExcludesCompleted() {
        save("Active", TaskType.TODO, LocalDateTime.now().plusDays(1));
        AcademicTask completed = save("Done", TaskType.TODO, LocalDateTime.now().plusDays(2));
        completed.setStatus(TaskStatus.COMPLETED);
        taskRepository.save(completed);

        List<AcademicTask> results = taskRepository.findByStatusNotOrderByDueDateAsc(TaskStatus.COMPLETED);

        assertThat(results).extracting(AcademicTask::getTitle).containsExactly("Active");
    }

    @Test
    void findByCanvasAssignmentIdReturnsMatchingTask() {
        AcademicTask task = save("Canvas task", TaskType.ASSIGNMENT, LocalDateTime.now());
        task.setCanvasAssignmentId(555L);
        taskRepository.save(task);

        Optional<AcademicTask> result = taskRepository.findByCanvasAssignmentId(555L);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Canvas task");
    }

    private AcademicTask save(String title, TaskType type, LocalDateTime dueDate) {
        AcademicTask task = new AcademicTask();
        task.setTitle(title);
        task.setType(type);
        task.setStatus(TaskStatus.PENDING);
        task.setDueDate(dueDate);
        return taskRepository.save(task);
    }
}
