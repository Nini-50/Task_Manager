package com.academictaskmanager.service;

import com.academictaskmanager.model.Note;
import com.academictaskmanager.model.User;
import com.academictaskmanager.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteService {

    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public List<Note> findByCourse(Long courseId, User owner) {
        return noteRepository.findByCourseIdAndOwnerOrderByPositionAscCreatedAtAsc(courseId, owner);
    }

    public Note findById(Long id, User owner) {
        return noteRepository.findByIdAndOwner(id, owner).orElseThrow(() ->
                new IllegalArgumentException("Note not found: " + id));
    }

    public Note save(Note note, User owner) {
        note.setOwner(owner);
        if (note.getId() == null && note.getCourse() != null) {
            // New notes are appended after any existing ones for the course, keeping tab order stable.
            note.setPosition(noteRepository.countByCourseIdAndOwner(note.getCourse().getId(), owner));
        }
        return noteRepository.save(note);
    }

    public void delete(Long id, User owner) {
        Note note = findById(id, owner);
        noteRepository.delete(note);
    }
}
