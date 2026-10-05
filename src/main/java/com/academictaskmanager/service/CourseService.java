package com.academictaskmanager.service;

import com.academictaskmanager.model.Course;
import com.academictaskmanager.model.Topic;
import com.academictaskmanager.model.User;
import com.academictaskmanager.repository.CourseRepository;
import com.academictaskmanager.repository.KeyTermRepository;
import com.academictaskmanager.repository.NoteRepository;
import com.academictaskmanager.repository.TopicRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final NoteRepository noteRepository;
    private final TopicRepository topicRepository;
    private final KeyTermRepository keyTermRepository;

    public CourseService(CourseRepository courseRepository, NoteRepository noteRepository,
                          TopicRepository topicRepository, KeyTermRepository keyTermRepository) {
        this.courseRepository = courseRepository;
        this.noteRepository = noteRepository;
        this.topicRepository = topicRepository;
        this.keyTermRepository = keyTermRepository;
    }

    public List<Course> findAll(User owner) {
        return courseRepository.findByOwner(owner);
    }

    public Course findById(Long id, User owner) {
        return courseRepository.findByIdAndOwner(id, owner).orElseThrow(() ->
                new IllegalArgumentException("Course not found: " + id));
    }

    public Course save(Course course, User owner) {
        course.setOwner(owner);
        return courseRepository.save(course);
    }

    /**
     * Deletes a course along with its notes and topics (and each topic's key terms). Tasks are
     * handled by JPA cascade on {@link Course#getTasks()}; notes and topics have no mapped
     * collection on {@link Course} (see {@link Topic} for why), so they must be cascaded manually.
     */
    public void delete(Long id, User owner) {
        Course course = findById(id, owner);
        noteRepository.deleteAll(noteRepository.findByCourseIdAndOwnerOrderByPositionAscCreatedAtAsc(id, owner));
        List<Topic> topics = topicRepository.findByCourseIdAndOwnerOrderByNameAsc(id, owner);
        topics.forEach(topic -> keyTermRepository.deleteAll(keyTermRepository.findByTopicAndOwner(topic, owner)));
        topicRepository.deleteAll(topics);
        courseRepository.delete(course);
    }
}
