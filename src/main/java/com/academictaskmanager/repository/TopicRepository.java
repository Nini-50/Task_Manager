package com.academictaskmanager.repository;

import com.academictaskmanager.model.Topic;
import com.academictaskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findByCourseIdAndOwnerOrderByNameAsc(Long courseId, User owner);
    Optional<Topic> findByIdAndOwner(Long id, User owner);
}
