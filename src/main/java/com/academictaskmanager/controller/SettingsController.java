package com.academictaskmanager.controller;

import com.academictaskmanager.model.UserSettings;
import com.academictaskmanager.service.SettingsService;
import org.springframework.web.bind.annotation.*;

/** Powers the "Customize" panel: theme color/mode, optional widgets, and Canvas connection. */
@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public UserSettings get() {
        return settingsService.getSettings();
    }

    @PutMapping
    public UserSettings update(@RequestBody UserSettings settings) {
        UserSettings existing = settingsService.getSettings();
        settings.setId(existing.getId());
        return settingsService.save(settings);
    }
}
