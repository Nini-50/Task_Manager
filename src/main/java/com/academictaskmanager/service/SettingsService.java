package com.academictaskmanager.service;

import com.academictaskmanager.model.UserSettings;
import com.academictaskmanager.repository.UserSettingsRepository;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    private final UserSettingsRepository settingsRepository;

    public SettingsService(UserSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    /** This MVP is single-user, so there is always exactly one settings row, created on first access. */
    public UserSettings getSettings() {
        return settingsRepository.findAll().stream().findFirst()
                .orElseGet(() -> settingsRepository.save(new UserSettings()));
    }

    public UserSettings save(UserSettings settings) {
        return settingsRepository.save(settings);
    }
}
