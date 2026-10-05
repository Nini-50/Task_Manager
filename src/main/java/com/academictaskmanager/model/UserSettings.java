package com.academictaskmanager.model;

import jakarta.persistence.*;

/**
 * Holds per-account customization preferences (theme color, which optional
 * widgets are enabled such as weather/sports). Each user account has exactly
 * one settings row.
 */
@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The account these settings belong to; one-to-one. */
    @OneToOne(optional = false)
    @JoinColumn(name = "owner_id", unique = true)
    private User owner;

    private String displayName = "Student";

    /** Dashboard theme: one of the named palette keys (e.g. "teal", "hot-pink")
     *  from the theme picker, or the default indigo accent (#4f46e5) if unset. */
    private String themeColor = "#4f46e5";

    private String themeMode = "light"; // light | dark

    // --- Optional dashboard widgets ---
    private boolean weatherWidgetEnabled = false;
    private String weatherLocation;

    private boolean sportsWidgetEnabled = false;
    private String sportsTeam;

    /** ESPN scoreboard path segment, e.g. "football/nfl" or "basketball/nba". */
    private String sportsLeague = "football/nfl";

    private boolean todoWidgetEnabled = true;
    private boolean calendarWidgetEnabled = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getThemeColor() { return themeColor; }
    public void setThemeColor(String themeColor) { this.themeColor = themeColor; }

    public String getThemeMode() { return themeMode; }
    public void setThemeMode(String themeMode) { this.themeMode = themeMode; }

    public boolean isWeatherWidgetEnabled() { return weatherWidgetEnabled; }
    public void setWeatherWidgetEnabled(boolean weatherWidgetEnabled) { this.weatherWidgetEnabled = weatherWidgetEnabled; }

    public String getWeatherLocation() { return weatherLocation; }
    public void setWeatherLocation(String weatherLocation) { this.weatherLocation = weatherLocation; }

    public boolean isSportsWidgetEnabled() { return sportsWidgetEnabled; }
    public void setSportsWidgetEnabled(boolean sportsWidgetEnabled) { this.sportsWidgetEnabled = sportsWidgetEnabled; }

    public String getSportsTeam() { return sportsTeam; }
    public void setSportsTeam(String sportsTeam) { this.sportsTeam = sportsTeam; }

    public String getSportsLeague() { return sportsLeague; }
    public void setSportsLeague(String sportsLeague) { this.sportsLeague = sportsLeague; }

    public boolean isTodoWidgetEnabled() { return todoWidgetEnabled; }
    public void setTodoWidgetEnabled(boolean todoWidgetEnabled) { this.todoWidgetEnabled = todoWidgetEnabled; }

    public boolean isCalendarWidgetEnabled() { return calendarWidgetEnabled; }
    public void setCalendarWidgetEnabled(boolean calendarWidgetEnabled) { this.calendarWidgetEnabled = calendarWidgetEnabled; }
}
