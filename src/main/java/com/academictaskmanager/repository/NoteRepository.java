package com.academictaskmanager.repository;

import com.academictaskmanager.model.Note;
import com.academictaskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByCourseIdAndOwnerOrderByPositionAscCreatedAtAsc(Long courseId, User owner);
    Optional<Note> findByIdAndOwner(Long id, User owner);
    int countByCourseIdAndOwner(Long courseId, User owner);
}
