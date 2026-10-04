package com.academictaskmanager.controller;

import com.academictaskmanager.model.UserSettings;
import com.academictaskmanager.service.CanvasSyncService;
import com.academictaskmanager.service.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Triggers a manual Canvas LMS sync using the student's saved Canvas settings. */
@RestController
@RequestMapping("/api/canvas")
public class CanvasSyncController {

    private final CanvasSyncService canvasSyncService;
    private final SettingsService settingsService;

    public CanvasSyncController(CanvasSyncService canvasSyncService, SettingsService settingsService) {
        this.canvasSyncService = canvasSyncService;
        this.settingsService = settingsService;
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> sync() {
        UserSettings settings = settingsService.getSettings();
        try {
            int count = canvasSyncService.sync(settings);
            return ResponseEntity.ok(Map.of("status", "ok", "tasksSynced", count));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("status", "error",
                    "message", "Canvas sync failed: " + e.getMessage()));
        }
    }
}
