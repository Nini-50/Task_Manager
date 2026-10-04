package com.academictaskmanager.service;

import com.academictaskmanager.model.User;
import com.academictaskmanager.model.UserSettings;
import com.academictaskmanager.repository.UserSettingsRepository;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    private final UserSettingsRepository settingsRepository;

    public SettingsService(UserSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    /** Each account has exactly one settings row, created lazily on first access. */
    public UserSettings getSettings(User owner) {
        return settingsRepository.findByOwner(owner)
                .orElseGet(() -> {
                    UserSettings settings = new UserSettings();
                    settings.setOwner(owner);
                    return settingsRepository.save(settings);
                });
    }

    public UserSettings save(UserSettings settings, User owner) {
        settings.setOwner(owner);
        return settingsRepository.save(settings);
    }
}
