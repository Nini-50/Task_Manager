package com.academictaskmanager.service;

import com.academictaskmanager.model.UserSettings;
import com.academictaskmanager.repository.AcademicTaskRepository;
import com.academictaskmanager.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class CanvasSyncServiceTest {

    @Mock
    private RestClient restClient;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private AcademicTaskRepository taskRepository;

    @Test
    void syncRequiresCanvasBaseUrlAndToken() {
        CanvasSyncService service = new CanvasSyncService(restClient, courseRepository, taskRepository);
        UserSettings settings = new UserSettings();

        assertThatThrownBy(() -> service.sync(settings))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Canvas base URL");
    }

    @Test
    void syncRequiresTokenEvenWhenBaseUrlPresent() {
        CanvasSyncService service = new CanvasSyncService(restClient, courseRepository, taskRepository);
        UserSettings settings = new UserSettings();
        settings.setCanvasBaseUrl("https://school.instructure.com");

        assertThatThrownBy(() -> service.sync(settings))
                .isInstanceOf(IllegalStateException.class);
    }
}
