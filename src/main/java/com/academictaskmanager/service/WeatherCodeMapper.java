package com.academictaskmanager.service;

import java.util.Map;

/**
 * Maps Open-Meteo's WMO weather codes to a human-readable description and an
 * emoji for the dashboard widget. See https://open-meteo.com/en/docs for the
 * full WMO code table; this covers the common cases.
 */
final class WeatherCodeMapper {

    private record Condition(String description, String dayEmoji, String nightEmoji) { }

    private static final Map<Integer, Condition> CODES = Map.ofEntries(
            Map.entry(0, new Condition("Clear sky", "☀️", "🌙")),
            Map.entry(1, new Condition("Mainly clear", "🌤️", "🌙")),
            Map.entry(2, new Condition("Partly cloudy", "⛅", "☁️")),
            Map.entry(3, new Condition("Overcast", "☁️", "☁️")),
            Map.entry(45, new Condition("Fog", "🌫️", "🌫️")),
            Map.entry(48, new Condition("Depositing rime fog", "🌫️", "🌫️")),
            Map.entry(51, new Condition("Light drizzle", "🌦️", "🌧️")),
            Map.entry(53, new Condition("Moderate drizzle", "🌦️", "🌧️")),
            Map.entry(55, new Condition("Dense drizzle", "🌧️", "🌧️")),
            Map.entry(61, new Condition("Slight rain", "🌦️", "🌧️")),
            Map.entry(63, new Condition("Moderate rain", "🌧️", "🌧️")),
            Map.entry(65, new Condition("Heavy rain", "🌧️", "🌧️")),
            Map.entry(66, new Condition("Freezing rain", "🌧️", "🌧️")),
            Map.entry(67, new Condition("Heavy freezing rain", "🌧️", "🌧️")),
            Map.entry(71, new Condition("Slight snow", "🌨️", "🌨️")),
            Map.entry(73, new Condition("Moderate snow", "🌨️", "🌨️")),
            Map.entry(75, new Condition("Heavy snow", "❄️", "❄️")),
            Map.entry(77, new Condition("Snow grains", "🌨️", "🌨️")),
            Map.entry(80, new Condition("Slight rain showers", "🌦️", "🌧️")),
            Map.entry(81, new Condition("Moderate rain showers", "🌧️", "🌧️")),
            Map.entry(82, new Condition("Violent rain showers", "⛈️", "⛈️")),
            Map.entry(85, new Condition("Slight snow showers", "🌨️", "🌨️")),
            Map.entry(86, new Condition("Heavy snow showers", "❄️", "❄️")),
            Map.entry(95, new Condition("Thunderstorm", "⛈️", "⛈️")),
            Map.entry(96, new Condition("Thunderstorm with slight hail", "⛈️", "⛈️")),
            Map.entry(99, new Condition("Thunderstorm with heavy hail", "⛈️", "⛈️"))
    );

    private static final Condition UNKNOWN = new Condition("Unknown conditions", "🌡️", "🌡️");

    private WeatherCodeMapper() { }

    static String description(int code) {
        return CODES.getOrDefault(code, UNKNOWN).description();
    }

    static String emoji(int code, boolean isDay) {
        Condition c = CODES.getOrDefault(code, UNKNOWN);
        return isDay ? c.dayEmoji() : c.nightEmoji();
    }
}
