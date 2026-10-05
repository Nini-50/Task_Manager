package com.academictaskmanager.service;

import com.academictaskmanager.model.Course;
import com.academictaskmanager.model.Note;
import com.academictaskmanager.model.User;
import com.academictaskmanager.repository.NoteRepository;
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
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    private NoteService noteService;
    private User owner;
    private Course course;

    @BeforeEach
    void setUp() {
        noteService = new NoteService(noteRepository);
        owner = new User();
        owner.setId(1L);
        owner.setUsername("alice");
        course = new Course();
        course.setId(7L);
        course.setName("Intro to CS");
    }

    @Test
    void findByCourseDelegatesToRepository() {
        Note note = new Note();
        note.setTitle("Lecture 1");
        when(noteRepository.findByCourseIdAndOwnerOrderByPositionAscCreatedAtAsc(7L, owner)).thenReturn(List.of(note));

        assertThat(noteService.findByCourse(7L, owner)).containsExactly(note);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(noteRepository.findByIdAndOwner(5L, owner)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.findById(5L, owner))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5");
    }

    @Test
    void savingNewNoteAppendsAfterExistingOnes() {
        Note note = new Note();
        note.setTitle("Midterm review");
        note.setCourse(course);
        when(noteRepository.countByCourseIdAndOwner(7L, owner)).thenReturn(2);
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note saved = noteService.save(note, owner);

        assertThat(saved.getPosition()).isEqualTo(2);
        assertThat(saved.getOwner()).isEqualTo(owner);
    }

    @Test
    void savingExistingNoteDoesNotRecomputePosition() {
        Note note = new Note();
        note.setId(3L);
        note.setTitle("Lecture 1");
        note.setCourse(course);
        note.setPosition(5);
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note saved = noteService.save(note, owner);

        assertThat(saved.getPosition()).isEqualTo(5);
        verify(noteRepository, never()).countByCourseIdAndOwner(anyLong(), any());
    }

    @Test
    void deleteDelegatesToRepository() {
        Note note = new Note();
        note.setId(3L);
        when(noteRepository.findByIdAndOwner(3L, owner)).thenReturn(Optional.of(note));

        noteService.delete(3L, owner);

        verify(noteRepository).delete(note);
    }
}
