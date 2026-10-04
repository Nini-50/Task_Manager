package com.academictaskmanager.dto;

/** Simplified game summary returned by our own `/api/widgets/sports` endpoint. */
public class SportsGameDto {
    private String shortName;
    private String statusDetail;
    private boolean completed;
    private String homeTeam;
    private String homeScore;
    private String awayTeam;
    private String awayScore;
    private boolean favorite;
    /** ISO-8601 kickoff/tip-off time; only populated for team-schedule lookups (previous/next game). */
    private String date;
    /** Link to this game's page on espn.com, so students can click through for full coverage. */
    private String espnUrl;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getEspnUrl() { return espnUrl; }
    public void setEspnUrl(String espnUrl) { this.espnUrl = espnUrl; }

    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }

    public String getStatusDetail() { return statusDetail; }
    public void setStatusDetail(String statusDetail) { this.statusDetail = statusDetail; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getHomeTeam() { return homeTeam; }
    public void setHomeTeam(String homeTeam) { this.homeTeam = homeTeam; }

    public String getHomeScore() { return homeScore; }
    public void setHomeScore(String homeScore) { this.homeScore = homeScore; }

    public String getAwayTeam() { return awayTeam; }
    public void setAwayTeam(String awayTeam) { this.awayTeam = awayTeam; }

    public String getAwayScore() { return awayScore; }
    public void setAwayScore(String awayScore) { this.awayScore = awayScore; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
}
