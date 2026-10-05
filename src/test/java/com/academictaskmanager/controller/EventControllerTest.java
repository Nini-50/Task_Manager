package com.academictaskmanager.controller;

import com.academictaskmanager.model.Event;
import com.academictaskmanager.model.User;
import com.academictaskmanager.service.EventService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
@WithMockUser(username = "alice")
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

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
    void upcomingReturnsEvents() throws Exception {
        Event event = new Event();
        event.setTitle("Study group");
        when(eventService.findUpcoming(eq(owner), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(event));

        mockMvc.perform(get("/api/events/upcoming")
                        .param("start", "2026-10-01T00:00:00")
                        .param("end", "2026-11-01T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Study group"));
    }

    @Test
    void createIgnoresClientSuppliedId() throws Exception {
        Event input = new Event();
        input.setId(99L);
        input.setTitle("Office hours");
        input.setStartDateTime(LocalDateTime.of(2026, 10, 15, 14, 0));
        Event saved = new Event();
        saved.setId(1L);
        saved.setTitle("Office hours");
        when(eventService.save(any(Event.class), eq(owner))).thenReturn(saved);

        mockMvc.perform(post("/api/events")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(eventService).save(argThat(e -> e.getId() == null), eq(owner));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/events/5").with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        verify(eventService).delete(5L, owner);
    }
}
