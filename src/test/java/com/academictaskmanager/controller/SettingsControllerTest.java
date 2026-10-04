package com.academictaskmanager.controller;

import com.academictaskmanager.model.UserSettings;
import com.academictaskmanager.service.SettingsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SettingsController.class)
class SettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SettingsService settingsService;

    @Test
    void getReturnsCurrentSettings() throws Exception {
        UserSettings settings = new UserSettings();
        settings.setDisplayName("Alex");
        when(settingsService.getSettings()).thenReturn(settings);

        mockMvc.perform(get("/api/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Alex"));
    }

    @Test
    void updatePreservesExistingIdAndSaves() throws Exception {
        UserSettings existing = new UserSettings();
        existing.setId(1L);
        when(settingsService.getSettings()).thenReturn(existing);
        when(settingsService.save(any(UserSettings.class))).thenAnswer(inv -> inv.getArgument(0));

        UserSettings incoming = new UserSettings();
        incoming.setThemeColor("#ff0000");

        mockMvc.perform(put("/api/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incoming)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.themeColor").value("#ff0000"));
    }
}
