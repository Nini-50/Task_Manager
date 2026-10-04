package com.academictaskmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    /** Plain RestClient; the Canvas base URL/token are applied per-request since they are user-configurable. */
    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }
}
