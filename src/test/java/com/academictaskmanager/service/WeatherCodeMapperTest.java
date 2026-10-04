package com.academictaskmanager.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherCodeMapperTest {

    @Test
    void knownCodeMapsToDescriptionAndDayNightEmoji() {
        assertThat(WeatherCodeMapper.description(0)).isEqualTo("Clear sky");
        assertThat(WeatherCodeMapper.emoji(0, true)).isEqualTo("☀️");
        assertThat(WeatherCodeMapper.emoji(0, false)).isEqualTo("🌙");
    }

    @Test
    void unknownCodeFallsBackToDefault() {
        assertThat(WeatherCodeMapper.description(12345)).isEqualTo("Unknown conditions");
        assertThat(WeatherCodeMapper.emoji(12345, true)).isEqualTo("🌡️");
    }
}
