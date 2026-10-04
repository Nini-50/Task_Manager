package com.academictaskmanager.dto;

/** The favorite team's most recent completed game and next scheduled game, or null for either if unknown. */
public class TeamScheduleDto {
    private SportsGameDto previousGame;
    private SportsGameDto nextGame;

    public SportsGameDto getPreviousGame() { return previousGame; }
    public void setPreviousGame(SportsGameDto previousGame) { this.previousGame = previousGame; }

    public SportsGameDto getNextGame() { return nextGame; }
    public void setNextGame(SportsGameDto nextGame) { this.nextGame = nextGame; }
}
