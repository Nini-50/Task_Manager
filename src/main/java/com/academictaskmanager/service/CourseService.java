package com.academictaskmanager.service;

import com.academictaskmanager.model.Course;
import com.academictaskmanager.model.User;
import com.academictaskmanager.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
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

    public void delete(Long id, User owner) {
        Course course = findById(id, owner);
        courseRepository.delete(course);
    }
}
