package com.academictaskmanager.repository;

import com.academictaskmanager.model.KeyTerm;
import com.academictaskmanager.model.Topic;
import com.academictaskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KeyTermRepository extends JpaRepository<KeyTerm, Long> {
    List<KeyTerm> findByTopicIdAndOwnerOrderByIdAsc(Long topicId, User owner);
    Optional<KeyTerm> findByIdAndOwner(Long id, User owner);
    List<KeyTerm> findByTopicAndOwner(Topic topic, User owner);
}
