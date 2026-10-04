package com.academictaskmanager.controller;

import com.academictaskmanager.model.AcademicTask;
import com.academictaskmanager.model.TaskStatus;
import com.academictaskmanager.model.TaskType;
import com.academictaskmanager.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @Test
    void getAllReturnsTasks() throws Exception {
        AcademicTask task = sampleTask();
        when(taskService.findAll()).thenReturn(List.of(task));

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
        when(taskService.save(any(AcademicTask.class))).thenReturn(saved);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(taskService).save(argThat(t -> t.getId() == null));
    }

    @Test
    void completeMarksTaskComplete() throws Exception {
        AcademicTask completed = sampleTask();
        completed.setId(7L);
        completed.setStatus(TaskStatus.COMPLETED);
        when(taskService.markComplete(7L)).thenReturn(completed);

        mockMvc.perform(post("/api/tasks/7/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/tasks/3"))
                .andExpect(status().isNoContent());

        verify(taskService).delete(3L);
    }

    @Test
    void upcomingPassesStartAndEndToService() throws Exception {
        when(taskService.findUpcoming(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/tasks/upcoming")
                        .param("start", "2026-10-01T00:00:00")
                        .param("end", "2026-10-15T00:00:00"))
                .andExpect(status().isOk());

        verify(taskService).findUpcoming(
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
}
