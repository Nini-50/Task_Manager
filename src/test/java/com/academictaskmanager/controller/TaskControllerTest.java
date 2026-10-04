package com.academictaskmanager.controller;

import com.academictaskmanager.model.AcademicTask;
import com.academictaskmanager.model.TaskStatus;
import com.academictaskmanager.model.TaskType;
import com.academictaskmanager.model.User;
import com.academictaskmanager.service.TaskService;
import com.academictaskmanager.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@WithMockUser(username = "alice")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

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
    void getAllReturnsTasks() throws Exception {
        AcademicTask task = sampleTask();
        when(taskService.findAll(owner)).thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Read chapter 3"));
    }

    @Test
    void createReturnsSavedTask() throws Exception {
        AcademicTask input = sampleTask();
        input.setId(42L); // client-supplied id must be ignored on create
        AcademicTask saved = sampleTask();
        saved.setId(1L);
        when(taskService.save(any(AcademicTask.class), eq(owner))).thenReturn(saved);

        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(taskService).save(argThat(t -> t.getId() == null), eq(owner));
    }

    @Test
    void completeMarksTaskComplete() throws Exception {
        AcademicTask completed = sampleTask();
        completed.setId(7L);
        completed.setStatus(TaskStatus.COMPLETED);
        when(taskService.markComplete(7L, owner)).thenReturn(completed);

        mockMvc.perform(post("/api/tasks/7/complete").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/tasks/3").with(csrf()))
                .andExpect(status().isNoContent());

        verify(taskService).delete(3L, owner);
    }

    @Test
    void upcomingPassesStartAndEndToService() throws Exception {
        when(taskService.findUpcoming(eq(owner), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/tasks/upcoming")
                        .param("start", "2026-10-01T00:00:00")
                        .param("end", "2026-10-15T00:00:00"))
                .andExpect(status().isOk());

        verify(taskService).findUpcoming(
                eq(owner),
                eq(LocalDateTime.parse("2026-10-01T00:00:00")),
                eq(LocalDateTime.parse("2026-10-15T00:00:00")));
    }

    private AcademicTask sampleTask() {
        AcademicTask task = new AcademicTask();
        task.setTitle("Read chapter 3");
        task.setType(TaskType.TODO);
        task.setStatus(TaskStatus.PENDING);
        task.setDueDate(LocalDateTime.of(2026, 10, 10, 23, 59));
        return task;
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor csrf() {
        return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf();
    }
}
