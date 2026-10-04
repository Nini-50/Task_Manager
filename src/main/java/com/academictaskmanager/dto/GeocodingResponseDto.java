package com.academictaskmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Response envelope from Open-Meteo's `GET /v1/search` geocoding endpoint. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeocodingResponseDto {
    private List<GeocodingResultDto> results;

    public List<GeocodingResultDto> getResults() { return results; }
    public void setResults(List<GeocodingResultDto> results) { this.results = results; }
}
