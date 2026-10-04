package com.academictaskmanager.service;

import com.academictaskmanager.model.User;
import com.academictaskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void registerEncodesPasswordAndSavesUser() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("supersecret")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.register("alice", "supersecret");

        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getPasswordHash()).isEqualTo("hashed");
    }

    @Test
    void registerRejectsDuplicateUsername() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> userService.register("alice", "supersecret"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("taken");
    }

    @Test
    void registerRejectsShortPassword() {
        assertThatThrownBy(() -> userService.register("alice", "short"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("8 characters");
    }

    @Test
    void registerRejectsBlankUsername() {
        assertThatThrownBy(() -> userService.register("   ", "supersecret"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username");
    }

    @Test
    void findByUsernameThrowsWhenMissing() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUsername("bob"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
