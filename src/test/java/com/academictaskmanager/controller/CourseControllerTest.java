package com.academictaskmanager.controller;

import com.academictaskmanager.model.Course;
import com.academictaskmanager.service.CourseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseService courseService;

    @Test
    void getAllReturnsCourses() throws Exception {
        Course course = new Course();
        course.setName("Intro to CS");
        when(courseService.findAll()).thenReturn(List.of(course));

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
        when(courseService.save(any(Course.class))).thenReturn(saved);

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(courseService).save(argThat(c -> c.getId() == null));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/courses/5"))
                .andExpect(status().isNoContent());

        verify(courseService).delete(5L);
    }
}
