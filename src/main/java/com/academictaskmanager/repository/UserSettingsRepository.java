package com.academictaskmanager.repository;

import com.academictaskmanager.model.User;
import com.academictaskmanager.model.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
    Optional<UserSettings> findByOwner(User owner);
}
