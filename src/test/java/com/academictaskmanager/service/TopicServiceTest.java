package com.academictaskmanager.service;

import com.academictaskmanager.model.Course;
import com.academictaskmanager.model.KeyTerm;
import com.academictaskmanager.model.Topic;
import com.academictaskmanager.model.User;
import com.academictaskmanager.repository.KeyTermRepository;
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
class TopicServiceTest {

    @Mock
    private TopicRepository topicRepository;
    @Mock
    private KeyTermRepository keyTermRepository;

    private TopicService topicService;
    private User owner;
    private Course course;

    @BeforeEach
    void setUp() {
        topicService = new TopicService(topicRepository, keyTermRepository);
        owner = new User();
        owner.setId(1L);
        owner.setUsername("alice");
        course = new Course();
        course.setId(7L);
        course.setName("Intro to CS");
    }

    @Test
    void findByCourseDelegatesToRepository() {
        Topic topic = new Topic();
        topic.setName("Recursion");
        when(topicRepository.findByCourseIdAndOwnerOrderByNameAsc(7L, owner)).thenReturn(List.of(topic));

        assertThat(topicService.findByCourse(7L, owner)).containsExactly(topic);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(topicRepository.findByIdAndOwner(5L, owner)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> topicService.findById(5L, owner))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5");
    }

    @Test
    void saveSetsOwner() {
        Topic topic = new Topic();
        topic.setName("Graphs");
        topic.setCourse(course);
        when(topicRepository.save(any(Topic.class))).thenAnswer(inv -> inv.getArgument(0));

        Topic saved = topicService.save(topic, owner);

        assertThat(saved.getOwner()).isEqualTo(owner);
    }

    @Test
    void deleteCascadesToKeyTerms() {
        Topic topic = new Topic();
        topic.setId(3L);
        KeyTerm term = new KeyTerm();
        term.setId(9L);
        when(topicRepository.findByIdAndOwner(3L, owner)).thenReturn(Optional.of(topic));
        when(keyTermRepository.findByTopicAndOwner(topic, owner)).thenReturn(List.of(term));

        topicService.delete(3L, owner);

        verify(keyTermRepository).deleteAll(List.of(term));
        verify(topicRepository).delete(topic);
    }
}
