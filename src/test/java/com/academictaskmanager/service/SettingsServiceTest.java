package com.academictaskmanager.service;

import com.academictaskmanager.model.User;
import com.academictaskmanager.model.UserSettings;
import com.academictaskmanager.repository.UserSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettingsServiceTest {

    @Mock
    private UserSettingsRepository settingsRepository;

    private SettingsService settingsService;
    private User owner;

    @BeforeEach
    void setUp() {
        settingsService = new SettingsService(settingsRepository);
        owner = new User();
        owner.setId(1L);
        owner.setUsername("alice");
    }

    @Test
    void getSettingsReturnsExistingRowWhenPresent() {
        UserSettings existing = new UserSettings();
        existing.setId(1L);
        when(settingsRepository.findByOwner(owner)).thenReturn(Optional.of(existing));

        UserSettings result = settingsService.getSettings(owner);

        assertThat(result).isSameAs(existing);
        verify(settingsRepository, never()).save(any());
    }

    @Test
    void getSettingsCreatesDefaultRowWhenNoneExists() {
        when(settingsRepository.findByOwner(owner)).thenReturn(Optional.empty());
        when(settingsRepository.save(any(UserSettings.class))).thenAnswer(inv -> inv.getArgument(0));

        UserSettings result = settingsService.getSettings(owner);

        assertThat(result).isNotNull();
        assertThat(result.getThemeColor()).isEqualTo("#4f46e5");
        verify(settingsRepository).save(any(UserSettings.class));
    }
}
