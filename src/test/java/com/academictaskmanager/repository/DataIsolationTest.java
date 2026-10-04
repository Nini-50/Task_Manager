package com.academictaskmanager.repository;

import com.academictaskmanager.model.AcademicTask;
import com.academictaskmanager.model.Course;
import com.academictaskmanager.model.TaskStatus;
import com.academictaskmanager.model.TaskType;
import com.academictaskmanager.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/** Confirms that one student's courses/tasks are never visible to another account. */
@DataJpaTest
class DataIsolationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private AcademicTaskRepository taskRepository;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        alice = userRepository.save(newUser("alice"));
        bob = userRepository.save(newUser("bob"));
    }

    @Test
    void courseLookupByIdAndOwnerExcludesOtherUsersCourses() {
        Course aliceCourse = new Course();
        aliceCourse.setName("Alice's Course");
        aliceCourse.setOwner(alice);
        aliceCourse = courseRepository.save(aliceCourse);

        assertThat(courseRepository.findByIdAndOwner(aliceCourse.getId(), bob)).isEmpty();
        assertThat(courseRepository.findByIdAndOwner(aliceCourse.getId(), alice)).isPresent();
        assertThat(courseRepository.findByOwner(bob)).isEmpty();
    }

    @Test
    void taskLookupByIdAndOwnerExcludesOtherUsersTasks() {
        AcademicTask aliceTask = new AcademicTask();
        aliceTask.setTitle("Alice's task");
        aliceTask.setType(TaskType.TODO);
        aliceTask.setStatus(TaskStatus.PENDING);
        aliceTask.setDueDate(LocalDateTime.now().plusDays(1));
        aliceTask.setOwner(alice);
        aliceTask = taskRepository.save(aliceTask);

        assertThat(taskRepository.findByIdAndOwner(aliceTask.getId(), bob)).isEmpty();
        assertThat(taskRepository.findByIdAndOwner(aliceTask.getId(), alice)).isPresent();
        assertThat(taskRepository.findByOwnerOrderByDueDateAsc(bob)).isEmpty();
    }

    private User newUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("hash");
        return user;
    }
}
