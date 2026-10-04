package com.academictaskmanager.service;

import com.academictaskmanager.dto.EspnCompetitionDto;
import com.academictaskmanager.dto.EspnCompetitorDto;
import com.academictaskmanager.dto.EspnEventDto;
import com.academictaskmanager.dto.EspnScoreboardDto;
import com.academictaskmanager.dto.SportsGameDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Pulls today's scoreboard for a league from ESPN's public, unofficial, no-API-key
 * endpoint (https://site.api.espn.com/apis/site/v2/sports/{league}/scoreboard) and,
 * if the student has a favorite team set, flags matching games.
 */
@Service
public class SportsService {

    /** Default league used when a student hasn't picked one yet. */
    public static final String DEFAULT_LEAGUE = "football/nfl";

    /** Allow-list of league path segments, matching the dashboard's league dropdown options. */
    private static final Set<String> ALLOWED_LEAGUES = Set.of(
            "football/nfl", "basketball/nba", "baseball/mlb", "hockey/nhl",
            "football/college-football", "basketball/mens-college-basketball", "soccer/eng.1");

    private static final String SCOREBOARD_BASE_URL = "https://site.api.espn.com/apis/site/v2/sports/";
    private static final int MAX_GAMES = 6;

    private final RestClient restClient;

    public SportsService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<SportsGameDto> getScoreboard(String league, String favoriteTeam) {
        String leaguePath = (league == null || league.isBlank()) ? DEFAULT_LEAGUE : league;
        if (!ALLOWED_LEAGUES.contains(leaguePath)) {
            leaguePath = DEFAULT_LEAGUE;
        }

        EspnScoreboardDto board;
        try {
            // Built manually (not via a {league} URI template variable) because the league path
            // itself contains a slash (e.g. "basketball/nba"); UriComponentsBuilder would percent-
            // encode it to %2F, which ESPN's routing rejects with a 400.
            URI uri = URI.create(SCOREBOARD_BASE_URL + leaguePath + "/scoreboard");
            board = restClient.get().uri(uri).retrieve().body(EspnScoreboardDto.class);
        } catch (RestClientException e) {
            throw new IllegalStateException("Couldn't reach ESPN for live scores right now.", e);
        }
        if (board == null || board.getEvents() == null || board.getEvents().isEmpty()) {
            return List.of();
        }

        String teamFilter = favoriteTeam == null ? "" : favoriteTeam.trim().toLowerCase();
        List<SportsGameDto> games = new ArrayList<>();
        for (EspnEventDto event : board.getEvents()) {
            SportsGameDto game = toGameDto(event, teamFilter);
            if (game != null) {
                games.add(game);
            }
        }

        if (!teamFilter.isEmpty()) {
            List<SportsGameDto> favorites = games.stream().filter(SportsGameDto::isFavorite).toList();
            if (!favorites.isEmpty()) {
                return favorites;
            }
        }
        return games.size() > MAX_GAMES ? games.subList(0, MAX_GAMES) : games;
    }

    private SportsGameDto toGameDto(EspnEventDto event, String teamFilter) {
        if (event.getCompetitions() == null || event.getCompetitions().isEmpty()) {
            return null;
        }
        EspnCompetitionDto competition = event.getCompetitions().get(0);
        if (competition.getCompetitors() == null) {
            return null;
        }

        SportsGameDto dto = new SportsGameDto();
        dto.setShortName(event.getShortName() != null ? event.getShortName() : event.getName());
        if (event.getStatus() != null && event.getStatus().getType() != null) {
            dto.setStatusDetail(event.getStatus().getType().getDescription());
            dto.setCompleted(event.getStatus().getType().isCompleted());
        }

        boolean favorite = false;
        for (EspnCompetitorDto competitor : competition.getCompetitors()) {
            boolean isHome = "home".equalsIgnoreCase(competitor.getHomeAway());
            String teamName = competitor.getTeam() != null ? competitor.getTeam().getDisplayName() : "Unknown";
            if (isHome) {
                dto.setHomeTeam(teamName);
                dto.setHomeScore(competitor.getScore());
            } else {
                dto.setAwayTeam(teamName);
                dto.setAwayScore(competitor.getScore());
            }
            if (!teamFilter.isEmpty() && matchesTeam(competitor, teamFilter)) {
                favorite = true;
            }
        }
        dto.setFavorite(favorite);
        return dto;
    }

    private boolean matchesTeam(EspnCompetitorDto competitor, String teamFilter) {
        if (competitor.getTeam() == null) return false;
        var team = competitor.getTeam();
        return containsIgnoreCase(team.getDisplayName(), teamFilter)
                || containsIgnoreCase(team.getShortDisplayName(), teamFilter)
                || containsIgnoreCase(team.getAbbreviation(), teamFilter);
    }

    private boolean containsIgnoreCase(String value, String filter) {
        return value != null && value.toLowerCase().contains(filter);
    }
}
