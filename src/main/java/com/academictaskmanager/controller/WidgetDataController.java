package com.academictaskmanager.controller;

import com.academictaskmanager.model.User;
import com.academictaskmanager.model.UserSettings;
import com.academictaskmanager.service.SettingsService;
import com.academictaskmanager.service.SportsService;
import com.academictaskmanager.service.UserService;
import com.academictaskmanager.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Powers the optional dashboard widgets: live weather (via Open-Meteo, no API
 * key required) and live sports scores (via ESPN's public scoreboard
 * endpoint, no API key required) for the settings the student has saved.
 */
@RestController
@RequestMapping("/api/widgets")
public class WidgetDataController {

    private final WeatherService weatherService;
    private final SportsService sportsService;
    private final SettingsService settingsService;
    private final UserService userService;

    public WidgetDataController(WeatherService weatherService, SportsService sportsService,
                                 SettingsService settingsService, UserService userService) {
        this.weatherService = weatherService;
        this.sportsService = sportsService;
        this.settingsService = settingsService;
        this.userService = userService;
    }

    private User currentUser(Authentication authentication) {
        return userService.findByUsername(authentication.getName());
    }

    @GetMapping("/weather")
    public ResponseEntity<Map<String, Object>> weather(Authentication authentication) {
        UserSettings settings = settingsService.getSettings(currentUser(authentication));
        try {
            return ResponseEntity.ok(Map.of("status", "ok",
                    "weather", weatherService.getCurrentWeather(settings.getWeatherLocation())));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("status", "error",
                    "message", "Couldn't load the weather right now."));
        }
    }

    @GetMapping("/sports")
    public ResponseEntity<Map<String, Object>> sports(Authentication authentication) {
        UserSettings settings = settingsService.getSettings(currentUser(authentication));
        try {
            String favoriteTeam = settings.getSportsTeam();
            if (favoriteTeam != null && !favoriteTeam.isBlank()) {
                com.academictaskmanager.dto.TeamScheduleDto schedule =
                        sportsService.getTeamSchedule(settings.getSportsLeague(), favoriteTeam);
                if (schedule != null) {
                    Map<String, Object> body = new java.util.HashMap<>();
                    body.put("status", "ok");
                    body.put("previousGame", schedule.getPreviousGame());
                    body.put("nextGame", schedule.getNextGame());
                    return ResponseEntity.ok(body);
                }
            }
            List<com.academictaskmanager.dto.SportsGameDto> games =
                    sportsService.getScoreboard(settings.getSportsLeague(), favoriteTeam);
            return ResponseEntity.ok(Map.of("status", "ok", "games", games));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("status", "error",
                    "message", "Couldn't load live scores right now."));
        }
    }
}
