package com.academictaskmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The `current` block from Open-Meteo's `GET /v1/forecast` response. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenMeteoCurrentDto {

    @JsonProperty("temperature_2m")
    private double temperature2m;

    @JsonProperty("weather_code")
    private int weatherCode;

    @JsonProperty("wind_speed_10m")
    private double windSpeed10m;

    @JsonProperty("is_day")
    private int isDay;

    public double getTemperature2m() { return temperature2m; }
    public void setTemperature2m(double temperature2m) { this.temperature2m = temperature2m; }

    public int getWeatherCode() { return weatherCode; }
    public void setWeatherCode(int weatherCode) { this.weatherCode = weatherCode; }

    public double getWindSpeed10m() { return windSpeed10m; }
    public void setWindSpeed10m(double windSpeed10m) { this.windSpeed10m = windSpeed10m; }

    public int getIsDay() { return isDay; }
    public void setIsDay(int isDay) { this.isDay = isDay; }
}
