package com.academictaskmanager.repository;

import com.academictaskmanager.model.AcademicTask;
import com.academictaskmanager.model.TaskStatus;
import com.academictaskmanager.model.TaskType;
import com.academictaskmanager.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AcademicTaskRepositoryTest {

    @Autowired
    private AcademicTaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername("alice");
        user.setPasswordHash("hash");
        owner = userRepository.save(user);
    }

    @Test
    void findByOwnerAndDueDateBetweenOrderByDueDateAscReturnsOnlyTasksInWindow() {
        save("Due before window", TaskType.TODO, LocalDateTime.of(2026, 9, 1, 0, 0));
        AcademicTask inWindowEarly = save("In window, earlier", TaskType.ASSIGNMENT, LocalDateTime.of(2026, 10, 5, 0, 0));
        AcademicTask inWindowLater = save("In window, later", TaskType.QUIZ, LocalDateTime.of(2026, 10, 10, 0, 0));
        save("Due after window", TaskType.EXAM, LocalDateTime.of(2026, 11, 1, 0, 0));

        List<AcademicTask> results = taskRepository.findByOwnerAndDueDateBetweenOrderByDueDateAsc(
                owner, LocalDateTime.of(2026, 10, 1, 0, 0), LocalDateTime.of(2026, 10, 31, 0, 0));

        assertThat(results).containsExactly(inWindowEarly, inWindowLater);
    }

    @Test
    void findByOwnerAndStatusNotOrderByDueDateAscExcludesCompleted() {
        save("Active", TaskType.TODO, LocalDateTime.now().plusDays(1));
        AcademicTask completed = save("Done", TaskType.TODO, LocalDateTime.now().plusDays(2));
        completed.setStatus(TaskStatus.COMPLETED);
        taskRepository.save(completed);

        List<AcademicTask> results = taskRepository.findByOwnerAndStatusNotOrderByDueDateAsc(owner, TaskStatus.COMPLETED);

        assertThat(results).extracting(AcademicTask::getTitle).containsExactly("Active");
    }

    private AcademicTask save(String title, TaskType type, LocalDateTime dueDate) {
        AcademicTask task = new AcademicTask();
        task.setTitle(title);
        task.setType(type);
        task.setStatus(TaskStatus.PENDING);
        task.setDueDate(dueDate);
        task.setOwner(owner);
        return taskRepository.save(task);
    }
}
