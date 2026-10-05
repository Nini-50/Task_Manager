package com.academictaskmanager.controller;

import com.academictaskmanager.model.Course;
import com.academictaskmanager.model.Note;
import com.academictaskmanager.model.User;
import com.academictaskmanager.service.NoteService;
import com.academictaskmanager.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoteController.class)
@WithMockUser(username = "alice")
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NoteService noteService;

    @MockBean
    private UserService userService;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setUsername("alice");
        when(userService.findByUsername("alice")).thenReturn(owner);
    }

    @Test
    void byCourseReturnsNotes() throws Exception {
        Note note = new Note();
        note.setTitle("Lecture 1");
        when(noteService.findByCourse(7L, owner)).thenReturn(List.of(note));

        mockMvc.perform(get("/api/notes").param("courseId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Lecture 1"));
    }

    @Test
    void createIgnoresClientSuppliedId() throws Exception {
        Course course = new Course();
        course.setId(7L);
        Note input = new Note();
        input.setId(99L);
        input.setTitle("Midterm review");
        input.setCourse(course);
        Note saved = new Note();
        saved.setId(1L);
        saved.setTitle("Midterm review");
        when(noteService.save(any(Note.class), eq(owner))).thenReturn(saved);

        mockMvc.perform(post("/api/notes")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(noteService).save(argThat(n -> n.getId() == null), eq(owner));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/notes/5").with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        verify(noteService).delete(5L, owner);
    }
}
