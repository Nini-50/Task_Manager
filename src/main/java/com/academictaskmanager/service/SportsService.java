package com.academictaskmanager.service;

import com.academictaskmanager.dto.EspnCompetitionDto;
import com.academictaskmanager.dto.EspnCompetitorDto;
import com.academictaskmanager.dto.EspnEventDto;
import com.academictaskmanager.dto.EspnScheduleResponseDto;
import com.academictaskmanager.dto.EspnScoreboardDto;
import com.academictaskmanager.dto.EspnStatusDto;
import com.academictaskmanager.dto.EspnTeamDto;
import com.academictaskmanager.dto.EspnTeamsResponseDto;
import com.academictaskmanager.dto.SportsGameDto;
import com.academictaskmanager.dto.TeamScheduleDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Pulls scores from ESPN's public, unofficial, no-API-key site API
 * (https://site.api.espn.com/apis/site/v2/sports/{league}/...). Two lookups are supported:
 * <ul>
 *   <li>{@link #getScoreboard}: today's games across a league, optionally flagging ones
 *       involving a favorite team.</li>
 *   <li>{@link #getTeamSchedule}: a specific team's most recent completed game and next
 *       scheduled game, resolved by looking up the team's ESPN id and reading its season
 *       schedule.</li>
 * </ul>
 */
@Service
public class SportsService {

    /** Default league used when a student hasn't picked one yet. */
    public static final String DEFAULT_LEAGUE = "football/nfl";

    /** Allow-list of league path segments, matching the dashboard's league dropdown options. */
    private static final Set<String> ALLOWED_LEAGUES = Set.of(
            "football/nfl", "basketball/nba", "baseball/mlb", "hockey/nhl",
            "football/college-football", "basketball/mens-college-basketball", "soccer/eng.1");

    private static final String SPORTS_BASE_URL = "https://site.api.espn.com/apis/site/v2/sports/";
    private static final int MAX_GAMES = 6;

    private final RestClient restClient;

    public SportsService(RestClient restClient) {
        this.restClient = restClient;
    }

    /** Normalizes a user-supplied league path against the allow-list, defending against path/URL injection. */
    private String normalizeLeague(String league) {
        String leaguePath = (league == null || league.isBlank()) ? DEFAULT_LEAGUE : league;
        return ALLOWED_LEAGUES.contains(leaguePath) ? leaguePath : DEFAULT_LEAGUE;
    }

    public List<SportsGameDto> getScoreboard(String league, String favoriteTeam) {
        String leaguePath = normalizeLeague(league);

        EspnScoreboardDto board;
        try {
            // Built manually (not via a {league} URI template variable) because the league path
            // itself contains a slash (e.g. "basketball/nba"); UriComponentsBuilder would percent-
            // encode it to %2F, which ESPN's routing rejects with a 400.
            URI uri = URI.create(SPORTS_BASE_URL + leaguePath + "/scoreboard");
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
            SportsGameDto game = toGameDto(event, teamFilter, leaguePath);
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

    /**
     * Looks up the favorite team's ESPN id, reads its season schedule, and returns the most
     * recent completed game plus the next scheduled one. Returns {@code null} (rather than an
     * empty result) when the team name can't be resolved, so callers can fall back to the
     * league-wide scoreboard instead of showing an empty "no games" state.
     */
    public TeamScheduleDto getTeamSchedule(String league, String favoriteTeam) {
        if (favoriteTeam == null || favoriteTeam.isBlank()) {
            return null;
        }
        String leaguePath = normalizeLeague(league);
        String teamId = findTeamId(leaguePath, favoriteTeam.trim());
        if (teamId == null) {
            return null;
        }

        EspnScheduleResponseDto schedule;
        try {
            URI uri = URI.create(SPORTS_BASE_URL + leaguePath + "/teams/" + teamId + "/schedule");
            schedule = restClient.get().uri(uri).retrieve().body(EspnScheduleResponseDto.class);
        } catch (RestClientException e) {
            throw new IllegalStateException("Couldn't reach ESPN for this team's schedule right now.", e);
        }
        if (schedule == null || schedule.getEvents() == null || schedule.getEvents().isEmpty()) {
            return new TeamScheduleDto();
        }

        List<EspnEventDto> sorted = schedule.getEvents().stream()
                .filter(e -> parseInstant(e.getDate()) != null)
                .sorted(Comparator.comparing(e -> parseInstant(e.getDate())))
                .toList();

        EspnEventDto previous = null;
        EspnEventDto next = null;
        for (EspnEventDto event : sorted) {
            if (isCompleted(event)) {
                previous = event;
            } else if (next == null) {
                next = event;
            }
        }

        TeamScheduleDto result = new TeamScheduleDto();
        String teamFilter = favoriteTeam.trim().toLowerCase();
        if (previous != null) {
            SportsGameDto dto = toGameDto(previous, teamFilter, leaguePath);
            if (dto != null) {
                dto.setFavorite(true);
                result.setPreviousGame(dto);
            }
        }
        if (next != null) {
            SportsGameDto dto = toGameDto(next, teamFilter, leaguePath);
            if (dto != null) {
                dto.setFavorite(true);
                result.setNextGame(dto);
            }
        }
        return result;
    }

    /** Finds the ESPN team id whose name/abbreviation matches the student's favorite-team text, or null. */
    private String findTeamId(String leaguePath, String favoriteTeam) {
        EspnTeamsResponseDto teamsResponse;
        try {
            URI uri = URI.create(SPORTS_BASE_URL + leaguePath + "/teams");
            teamsResponse = restClient.get().uri(uri).retrieve().body(EspnTeamsResponseDto.class);
        } catch (RestClientException e) {
            throw new IllegalStateException("Couldn't reach ESPN to look up that team right now.", e);
        }
        if (teamsResponse == null || teamsResponse.getSports() == null) {
            return null;
        }
        String filter = favoriteTeam.toLowerCase();
        for (EspnTeamsResponseDto.Sport sport : teamsResponse.getSports()) {
            if (sport.getLeagues() == null) continue;
            for (EspnTeamsResponseDto.League leagueEntry : sport.getLeagues()) {
                if (leagueEntry.getTeams() == null) continue;
                for (EspnTeamsResponseDto.Entry entry : leagueEntry.getTeams()) {
                    EspnTeamDto team = entry.getTeam();
                    if (team != null && matchesTeam(team, filter)) {
                        return team.getId();
                    }
                }
            }
        }
        return null;
    }

    // ESPN's event "date" field usually omits seconds (e.g. "2026-09-13T17:00Z"), which
    // java.time.Instant.parse rejects since it requires full ISO_INSTANT with seconds. When the
    // strict parse fails, fall back to parsing it as a UTC local date-time with optional seconds.
    private static final DateTimeFormatter ESPN_UTC_LOCAL_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm")
            .optionalStart()
            .appendPattern(":ss")
            .optionalEnd()
            .toFormatter();

    private Instant parseInstant(String date) {
        if (date == null) return null;
        try {
            return Instant.parse(date);
        } catch (Exception e) {
            if (date.endsWith("Z")) {
                try {
                    return ESPN_UTC_LOCAL_FORMATTER
                            .parse(date.substring(0, date.length() - 1), java.time.LocalDateTime::from)
                            .toInstant(java.time.ZoneOffset.UTC);
                } catch (Exception e2) {
                    return null;
                }
            }
            return null;
        }
    }

    /** True once ESPN marks the game "Final"; status lives on the event for scoreboard data and on
     * the competition for team-schedule data, so both locations are checked. */
    private boolean isCompleted(EspnEventDto event) {
        EspnStatusDto status = statusOf(event);
        return status != null && status.getType() != null && status.getType().isCompleted();
    }

    private EspnStatusDto statusOf(EspnEventDto event) {
        if (event.getStatus() != null) {
            return event.getStatus();
        }
        if (event.getCompetitions() != null && !event.getCompetitions().isEmpty()) {
            return event.getCompetitions().get(0).getStatus();
        }
        return null;
    }

    private SportsGameDto toGameDto(EspnEventDto event, String teamFilter, String leaguePath) {
        if (event.getCompetitions() == null || event.getCompetitions().isEmpty()) {
            return null;
        }
        EspnCompetitionDto competition = event.getCompetitions().get(0);
        if (competition.getCompetitors() == null) {
            return null;
        }

        SportsGameDto dto = new SportsGameDto();
        dto.setShortName(event.getShortName() != null ? event.getShortName() : event.getName());
        dto.setDate(event.getDate());
        dto.setEspnUrl(espnGameUrl(leaguePath, event.getId()));
        EspnStatusDto status = statusOf(event);
        if (status != null && status.getType() != null) {
            dto.setStatusDetail(status.getType().getDescription());
            dto.setCompleted(status.getType().isCompleted());
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
            if (!teamFilter.isEmpty() && competitor.getTeam() != null && matchesTeam(competitor.getTeam(), teamFilter)) {
                favorite = true;
            }
        }
        dto.setFavorite(favorite);
        return dto;
    }

    /**
     * Builds a link to this game's page on espn.com. ESPN's site URLs use just the sport's short
     * segment (e.g. "nfl"), not the full API league path (e.g. "football/nfl"), so the leading
     * segment is stripped off.
     */
    private String espnGameUrl(String leaguePath, String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return null;
        }
        String sportSegment = leaguePath.contains("/")
                ? leaguePath.substring(leaguePath.lastIndexOf('/') + 1)
                : leaguePath;
        return "https://www.espn.com/" + sportSegment + "/game/_/gameId/" + eventId;
    }

    private boolean matchesTeam(EspnTeamDto team, String teamFilter) {
        return containsIgnoreCase(team.getDisplayName(), teamFilter)
                || containsIgnoreCase(team.getShortDisplayName(), teamFilter)
                || containsIgnoreCase(team.getAbbreviation(), teamFilter);
    }

    private boolean containsIgnoreCase(String value, String filter) {
        return value != null && value.toLowerCase().contains(filter);
    }
}
