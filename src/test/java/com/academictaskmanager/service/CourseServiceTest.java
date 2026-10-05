package com.academictaskmanager.service;

import com.academictaskmanager.model.Course;
import com.academictaskmanager.model.KeyTerm;
import com.academictaskmanager.model.Note;
import com.academictaskmanager.model.Topic;
import com.academictaskmanager.model.User;
import com.academictaskmanager.repository.CourseRepository;
import com.academictaskmanager.repository.KeyTermRepository;
import com.academictaskmanager.repository.NoteRepository;
import com.academictaskmanager.repository.TopicRepository;
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
    @Mock
    private NoteRepository noteRepository;
    @Mock
    private TopicRepository topicRepository;
    @Mock
    private KeyTermRepository keyTermRepository;

    private CourseService courseService;
    private User owner;

    @BeforeEach
    void setUp() {
        courseService = new CourseService(courseRepository, noteRepository, topicRepository, keyTermRepository);
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
        Note note = new Note();
        note.setId(10L);
        Topic topic = new Topic();
        topic.setId(20L);
        KeyTerm term = new KeyTerm();
        term.setId(30L);
        when(courseRepository.findByIdAndOwner(3L, owner)).thenReturn(Optional.of(course));
        when(noteRepository.findByCourseIdAndOwnerOrderByPositionAscCreatedAtAsc(3L, owner)).thenReturn(List.of(note));
        when(topicRepository.findByCourseIdAndOwnerOrderByNameAsc(3L, owner)).thenReturn(List.of(topic));
        when(keyTermRepository.findByTopicAndOwner(topic, owner)).thenReturn(List.of(term));

        courseService.delete(3L, owner);

        verify(noteRepository).deleteAll(List.of(note));
        verify(keyTermRepository).deleteAll(List.of(term));
        verify(topicRepository).deleteAll(List.of(topic));
        verify(courseRepository).delete(course);
    }
}
