package com.academictaskmanager.controller;

import com.academictaskmanager.model.User;
import com.academictaskmanager.model.UserSettings;
import com.academictaskmanager.service.SettingsService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SettingsController.class)
@WithMockUser(username = "alice")
class SettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SettingsService settingsService;

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
    void getReturnsCurrentSettings() throws Exception {
        UserSettings settings = new UserSettings();
        settings.setDisplayName("Alex");
        when(settingsService.getSettings(owner)).thenReturn(settings);

        mockMvc.perform(get("/api/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Alex"));
    }

    @Test
    void updatePreservesExistingIdAndSaves() throws Exception {
        UserSettings existing = new UserSettings();
        existing.setId(1L);
        when(settingsService.getSettings(owner)).thenReturn(existing);
        when(settingsService.save(any(UserSettings.class), eq(owner))).thenAnswer(inv -> inv.getArgument(0));

        UserSettings incoming = new UserSettings();
        incoming.setThemeColor("#ff0000");

        mockMvc.perform(put("/api/settings")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incoming)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.themeColor").value("#ff0000"));
    }
}
