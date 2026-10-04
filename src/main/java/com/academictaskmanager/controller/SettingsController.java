package com.academictaskmanager.controller;

import com.academictaskmanager.model.User;
import com.academictaskmanager.model.UserSettings;
import com.academictaskmanager.service.SettingsService;
import com.academictaskmanager.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Powers the "Customize" panel: theme color/mode, optional widgets, and Canvas connection. */
@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;
    private final UserService userService;

    public SettingsController(SettingsService settingsService, UserService userService) {
        this.settingsService = settingsService;
        this.userService = userService;
    }

    private User currentUser(Authentication authentication) {
        return userService.findByUsername(authentication.getName());
    }

    @GetMapping
    public UserSettings get(Authentication authentication) {
        return settingsService.getSettings(currentUser(authentication));
    }

    @PutMapping
    public UserSettings update(Authentication authentication, @RequestBody UserSettings settings) {
        User owner = currentUser(authentication);
        UserSettings existing = settingsService.getSettings(owner);
        settings.setId(existing.getId());
        return settingsService.save(settings, owner);
    }
}
