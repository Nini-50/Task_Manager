package com.academictaskmanager.service;

import com.academictaskmanager.model.AcademicTask;
import com.academictaskmanager.model.TaskStatus;
import com.academictaskmanager.model.TaskType;
import com.academictaskmanager.model.User;
import com.academictaskmanager.repository.AcademicTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private AcademicTaskRepository taskRepository;

    private TaskService taskService;
    private User owner;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository);
        owner = new User();
        owner.setId(1L);
        owner.setUsername("alice");
    }

    @Test
    void markCompleteSetsStatusToCompletedAndSaves() {
        AcademicTask task = new AcademicTask();
        task.setId(1L);
        task.setStatus(TaskStatus.PENDING);
        when(taskRepository.findByIdAndOwner(1L, owner)).thenReturn(java.util.Optional.of(task));
        when(taskRepository.save(any(AcademicTask.class))).thenAnswer(inv -> inv.getArgument(0));

        AcademicTask result = taskService.markComplete(1L, owner);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        verify(taskRepository).save(task);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(taskRepository.findByIdAndOwner(99L, owner)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> taskService.findById(99L, owner))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findActiveTodosSortsByPriorityDescThenDueDateAsc() {
        AcademicTask low = taskWith(TaskType.TODO, 1, LocalDateTime.now().plusDays(1));
        AcademicTask urgentLater = taskWith(TaskType.TODO, 5, LocalDateTime.now().plusDays(5));
        AcademicTask urgentSooner = taskWith(TaskType.TODO, 5, LocalDateTime.now().plusDays(2));
        when(taskRepository.findByOwnerAndStatusNotOrderByDueDateAsc(owner, TaskStatus.COMPLETED))
                .thenReturn(List.of(low, urgentLater, urgentSooner));

        List<AcademicTask> result = taskService.findActiveTodos(owner);

        assertThat(result).containsExactly(urgentSooner, urgentLater, low);
    }

    private AcademicTask taskWith(TaskType type, int priority, LocalDateTime dueDate) {
        AcademicTask task = new AcademicTask();
        task.setType(type);
        task.setPriority(priority);
        task.setDueDate(dueDate);
        task.setStatus(TaskStatus.PENDING);
        return task;
    }
}
