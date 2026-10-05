package com.academictaskmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    /**
     * Plain RestClient shared by services that call external public APIs (e.g. weather, sports
     * scores). A browser-like User-Agent is set because some public APIs (e.g. ESPN's
     * scoreboard, which sits behind Akamai) reject the JVM's default "Java/x.y.z" User-Agent with
     * a 403.
     */
    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .defaultHeader("User-Agent",
                        "Mozilla/5.0 (compatible; AcademicTaskManager/1.0; +https://github.com/Nini-50/Task_Manager)")
                .build();
    }
}
