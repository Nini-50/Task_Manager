package com.academictaskmanager.dto;

/** Simplified current-conditions payload returned by our own `/api/widgets/weather` endpoint. */
public class WeatherDto {
    private String location;
    private double temperatureF;
    private double windMph;
    private String condition;
    private String emoji;
    private boolean day;

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getTemperatureF() { return temperatureF; }
    public void setTemperatureF(double temperatureF) { this.temperatureF = temperatureF; }

    public double getWindMph() { return windMph; }
    public void setWindMph(double windMph) { this.windMph = windMph; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public boolean isDay() { return day; }
    public void setDay(boolean day) { this.day = day; }
}
