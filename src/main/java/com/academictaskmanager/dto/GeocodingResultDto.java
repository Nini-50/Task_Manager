package com.academictaskmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** One match from Open-Meteo's free geocoding API (city name -&gt; lat/lon). */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeocodingResultDto {
    private String name;
    private String admin1;
    private String country;
    private double latitude;
    private double longitude;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAdmin1() { return admin1; }
    public void setAdmin1(String admin1) { this.admin1 = admin1; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
