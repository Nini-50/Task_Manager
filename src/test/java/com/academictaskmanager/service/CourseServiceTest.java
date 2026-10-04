package com.academictaskmanager.service;

import com.academictaskmanager.model.Course;
import com.academictaskmanager.model.User;
import com.academictaskmanager.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    private CourseService courseService;
    private User owner;

    @BeforeEach
    void setUp() {
        courseService = new CourseService(courseRepository);
        owner = new User();
        owner.setId(1L);
        owner.setUsername("alice");
    }

    @Test
    void findAllDelegatesToRepository() {
        Course course = new Course();
        course.setName("Intro to CS");
        when(courseRepository.findByOwner(owner)).thenReturn(List.of(course));

        assertThat(courseService.findAll(owner)).containsExactly(course);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(courseRepository.findByIdAndOwner(5L, owner)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.findById(5L, owner))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5");
    }

    @Test
    void deleteDelegatesToRepository() {
        Course course = new Course();
        course.setId(3L);
        when(courseRepository.findByIdAndOwner(3L, owner)).thenReturn(Optional.of(course));

        courseService.delete(3L, owner);

        verify(courseRepository).delete(course);
    }
}
