package com.academictaskmanager.repository;

import com.academictaskmanager.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCanvasCourseId(Long canvasCourseId);
}
