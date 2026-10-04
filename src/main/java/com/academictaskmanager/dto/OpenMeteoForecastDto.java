package com.academictaskmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response envelope from Open-Meteo's `GET /v1/forecast` endpoint. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenMeteoForecastDto {
    private OpenMeteoCurrentDto current;

    public OpenMeteoCurrentDto getCurrent() { return current; }
    public void setCurrent(OpenMeteoCurrentDto current) { this.current = current; }
}
