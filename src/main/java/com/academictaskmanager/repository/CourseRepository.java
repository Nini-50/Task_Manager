package com.academictaskmanager.repository;

import com.academictaskmanager.model.Course;
import com.academictaskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByOwner(User owner);
    Optional<Course> findByIdAndOwner(Long id, User owner);
}
