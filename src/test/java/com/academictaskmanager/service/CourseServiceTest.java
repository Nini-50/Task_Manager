package com.academictaskmanager.service;

import com.academictaskmanager.model.Course;
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

    @BeforeEach
    void setUp() {
        courseService = new CourseService(courseRepository);
    }

    @Test
    void findAllDelegatesToRepository() {
        Course course = new Course();
        course.setName("Intro to CS");
        when(courseRepository.findAll()).thenReturn(List.of(course));

        assertThat(courseService.findAll()).containsExactly(course);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(courseRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.findById(5L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5");
    }

    @Test
    void deleteDelegatesToRepository() {
        courseService.delete(3L);

        verify(courseRepository).deleteById(3L);
    }
}
