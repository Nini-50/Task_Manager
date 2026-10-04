package com.academictaskmanager.service;

import com.academictaskmanager.dto.GeocodingResponseDto;
import com.academictaskmanager.dto.GeocodingResultDto;
import com.academictaskmanager.dto.OpenMeteoForecastDto;
import com.academictaskmanager.dto.WeatherDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Looks up current weather conditions for a free-text location (e.g.
 * "Boston, MA") using Open-Meteo's free, no-API-key-required services:
 * geocoding (name -&gt; lat/lon) at https://open-meteo.com/en/docs/geocoding-api
 * and current conditions at https://open-meteo.com/en/docs.
 */
@Service
public class WeatherService {

    private static final String GEOCODE_URL =
            "https://geocoding-api.open-meteo.com/v1/search?name={name}&count=1&language=en&format=json";
    private static final String FORECAST_URL =
            "https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}"
                    + "&current=temperature_2m,weather_code,wind_speed_10m,is_day"
                    + "&temperature_unit=fahrenheit&wind_speed_unit=mph";

    private final RestClient restClient;

    public WeatherService(RestClient restClient) {
        this.restClient = restClient;
    }

    public WeatherDto getCurrentWeather(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalStateException("Set a location in settings to see the weather.");
        }

        GeocodingResponseDto geocoded;
        try {
            geocoded = restClient.get().uri(GEOCODE_URL, location).retrieve().body(GeocodingResponseDto.class);
        } catch (RestClientException e) {
            throw new IllegalStateException("Couldn't reach the weather service right now.", e);
        }
        if (geocoded == null || geocoded.getResults() == null || geocoded.getResults().isEmpty()) {
            throw new IllegalStateException("Couldn't find a location matching \"" + location + "\".");
        }
        GeocodingResultDto place = geocoded.getResults().get(0);

        OpenMeteoForecastDto forecast;
        try {
            forecast = restClient.get()
                    .uri(FORECAST_URL, place.getLatitude(), place.getLongitude())
                    .retrieve()
                    .body(OpenMeteoForecastDto.class);
        } catch (RestClientException e) {
            throw new IllegalStateException("Couldn't reach the weather service right now.", e);
        }
        if (forecast == null || forecast.getCurrent() == null) {
            throw new IllegalStateException("Weather data is temporarily unavailable.");
        }

        var current = forecast.getCurrent();
        boolean isDay = current.getIsDay() == 1;

        WeatherDto dto = new WeatherDto();
        dto.setLocation(formatLocation(place));
        dto.setTemperatureF(current.getTemperature2m());
        dto.setWindMph(current.getWindSpeed10m());
        dto.setDay(isDay);
        dto.setCondition(WeatherCodeMapper.description(current.getWeatherCode()));
        dto.setEmoji(WeatherCodeMapper.emoji(current.getWeatherCode(), isDay));
        return dto;
    }

    private String formatLocation(GeocodingResultDto place) {
        if (place.getAdmin1() != null && !place.getAdmin1().isBlank()) {
            return place.getName() + ", " + place.getAdmin1();
        }
        return place.getName();
    }
}
