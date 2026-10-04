package com.academictaskmanager.controller;

import com.academictaskmanager.model.Course;
import com.academictaskmanager.model.User;
import com.academictaskmanager.service.CourseService;
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

@WebMvcTest(CourseController.class)
@WithMockUser(username = "alice")
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseService courseService;

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
    void getAllReturnsCourses() throws Exception {
        Course course = new Course();
        course.setName("Intro to CS");
        when(courseService.findAll(owner)).thenReturn(List.of(course));

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Intro to CS"));
    }

    @Test
    void createIgnoresClientSuppliedId() throws Exception {
        Course input = new Course();
        input.setId(99L);
        input.setName("Biology 101");
        Course saved = new Course();
        saved.setId(1L);
        saved.setName("Biology 101");
        when(courseService.save(any(Course.class), eq(owner))).thenReturn(saved);

        mockMvc.perform(post("/api/courses")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(courseService).save(argThat(c -> c.getId() == null), eq(owner));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/courses/5").with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        verify(courseService).delete(5L, owner);
    }
}
