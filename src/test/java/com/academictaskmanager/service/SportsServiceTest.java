package com.academictaskmanager.service;

import com.academictaskmanager.dto.EspnCompetitionDto;
import com.academictaskmanager.dto.EspnCompetitorDto;
import com.academictaskmanager.dto.EspnEventDto;
import com.academictaskmanager.dto.EspnScheduleResponseDto;
import com.academictaskmanager.dto.EspnScoreboardDto;
import com.academictaskmanager.dto.EspnStatusDto;
import com.academictaskmanager.dto.EspnStatusTypeDto;
import com.academictaskmanager.dto.EspnTeamDto;
import com.academictaskmanager.dto.EspnTeamsResponseDto;
import com.academictaskmanager.dto.SportsGameDto;
import com.academictaskmanager.dto.TeamScheduleDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentMatchers;

@ExtendWith(MockitoExtension.class)
class SportsServiceTest {

    private EspnEventDto game(String shortName, String homeTeam, String homeScore,
                               String awayTeam, String awayScore) {
        return scheduleEvent(shortName, null, homeTeam, homeScore, awayTeam, awayScore, true);
    }

    /**
     * Builds a schedule-style event: status lives on the competition (not the event), matching
     * ESPN's team-schedule response shape rather than its scoreboard shape.
     */
    private EspnEventDto scheduleEvent(String shortName, String date, String homeTeam, String homeScore,
                                        String awayTeam, String awayScore, boolean completed) {
        EspnTeamDto home = new EspnTeamDto();
        home.setDisplayName(homeTeam);
        home.setShortDisplayName(homeTeam);
        home.setAbbreviation(homeTeam.substring(0, Math.min(3, homeTeam.length())));

        EspnTeamDto away = new EspnTeamDto();
        away.setDisplayName(awayTeam);
        away.setShortDisplayName(awayTeam);
        away.setAbbreviation(awayTeam.substring(0, Math.min(3, awayTeam.length())));

        EspnCompetitorDto homeCompetitor = new EspnCompetitorDto();
        homeCompetitor.setHomeAway("home");
        homeCompetitor.setTeam(home);
        homeCompetitor.setScore(homeScore);

        EspnCompetitorDto awayCompetitor = new EspnCompetitorDto();
        awayCompetitor.setHomeAway("away");
        awayCompetitor.setTeam(away);
        awayCompetitor.setScore(awayScore);

        EspnStatusTypeDto type = new EspnStatusTypeDto();
        type.setDescription(completed ? "Final" : "Scheduled");
        type.setCompleted(completed);
        EspnStatusDto status = new EspnStatusDto();
        status.setType(type);

        EspnCompetitionDto competition = new EspnCompetitionDto();
        competition.setCompetitors(List.of(homeCompetitor, awayCompetitor));
        if (date != null) {
            // Schedule responses carry status on the competition rather than the event.
            competition.setStatus(status);
        }

        EspnEventDto event = new EspnEventDto();
        event.setShortName(shortName);
        event.setDate(date);
        event.setCompetitions(List.of(competition));
        if (date == null) {
            // Scoreboard responses carry status on the event itself.
            event.setStatus(status);
        }
        return event;
    }

    private RestClient restClientReturning(EspnScoreboardDto board) {
        RestClient restClient = mock(RestClient.class, Answers.RETURNS_DEEP_STUBS);
        when(restClient.get().uri(any(java.net.URI.class)).retrieve().body(EspnScoreboardDto.class))
                .thenReturn(board);
        return restClient;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private RestClient restClientForTeamSchedule(EspnTeamsResponseDto teams, EspnScheduleResponseDto schedule) {
        // The teams-list and schedule lookups share the restClient.get() entry point but differ in
        // URL and response type, so each chain is mocked separately and distinguished by URL content
        // (matching the established pattern for avoiding RETURNS_DEEP_STUBS strict-stubbing conflicts).
        RestClient restClient = mock(RestClient.class);
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        when(restClient.get()).thenReturn(uriSpec);

        RestClient.RequestHeadersSpec teamsHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec teamsResponseSpec = mock(RestClient.ResponseSpec.class);
        when(uriSpec.uri(ArgumentMatchers.<java.net.URI>argThat(uri -> uri != null && uri.toString().endsWith("/teams"))))
                .thenReturn(teamsHeadersSpec);
        when(teamsHeadersSpec.retrieve()).thenReturn(teamsResponseSpec);
        when(teamsResponseSpec.body(EspnTeamsResponseDto.class)).thenReturn(teams);

        if (schedule != null) {
            RestClient.RequestHeadersSpec scheduleHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
            RestClient.ResponseSpec scheduleResponseSpec = mock(RestClient.ResponseSpec.class);
            when(uriSpec.uri(ArgumentMatchers.<java.net.URI>argThat(uri -> uri != null && uri.toString().contains("/schedule"))))
                    .thenReturn(scheduleHeadersSpec);
            when(scheduleHeadersSpec.retrieve()).thenReturn(scheduleResponseSpec);
            when(scheduleResponseSpec.body(EspnScheduleResponseDto.class)).thenReturn(schedule);
        }
        return restClient;
    }

    private EspnTeamsResponseDto teamsResponseWith(String id, String displayName, String abbreviation) {
        EspnTeamDto team = new EspnTeamDto();
        team.setId(id);
        team.setDisplayName(displayName);
        team.setShortDisplayName(displayName);
        team.setAbbreviation(abbreviation);

        EspnTeamsResponseDto.Entry entry = new EspnTeamsResponseDto.Entry();
        entry.setTeam(team);
        EspnTeamsResponseDto.League league = new EspnTeamsResponseDto.League();
        league.setTeams(List.of(entry));
        EspnTeamsResponseDto.Sport sport = new EspnTeamsResponseDto.Sport();
        sport.setLeagues(List.of(league));
        EspnTeamsResponseDto response = new EspnTeamsResponseDto();
        response.setSports(List.of(sport));
        return response;
    }

    @Test
    void noEventsReturnsEmptyList() {
        EspnScoreboardDto board = new EspnScoreboardDto();
        board.setEvents(List.of());
        SportsService service = new SportsService(restClientReturning(board));

        assertThat(service.getScoreboard("football/nfl", null)).isEmpty();
    }

    @Test
    void favoriteTeamMatchIsFlaggedAndFiltersResults() {
        EspnScoreboardDto board = new EspnScoreboardDto();
        board.setEvents(List.of(
                game("BOS @ NYK", "New York Knicks", "101", "Boston Celtics", "99"),
                game("LAL @ GSW", "Golden State Warriors", "110", "Los Angeles Lakers", "108")
        ));
        SportsService service = new SportsService(restClientReturning(board));

        List<SportsGameDto> games = service.getScoreboard("basketball/nba", "Celtics");

        assertThat(games).hasSize(1);
        assertThat(games.get(0).isFavorite()).isTrue();
        assertThat(games.get(0).getAwayTeam()).isEqualTo("Boston Celtics");
        assertThat(games.get(0).getHomeScore()).isEqualTo("101");
    }

    @Test
    void noFavoriteMatchReturnsAllGamesUnfiltered() {
        EspnScoreboardDto board = new EspnScoreboardDto();
        board.setEvents(List.of(
                game("BOS @ NYK", "New York Knicks", "101", "Boston Celtics", "99")
        ));
        SportsService service = new SportsService(restClientReturning(board));

        List<SportsGameDto> games = service.getScoreboard("basketball/nba", "Lakers");

        assertThat(games).hasSize(1);
        assertThat(games.get(0).isFavorite()).isFalse();
    }

    @Test
    void blankLeagueFallsBackToDefault() {
        EspnScoreboardDto board = new EspnScoreboardDto();
        board.setEvents(List.of());
        SportsService service = new SportsService(restClientReturning(board));

        assertThat(service.getScoreboard(null, null)).isEmpty();
        assertThat(service.getScoreboard("  ", null)).isEmpty();
    }

    @Test
    void unrecognizedLeagueFallsBackToDefaultInsteadOfPassingThroughArbitraryInput() {
        EspnScoreboardDto board = new EspnScoreboardDto();
        board.setEvents(List.of());
        SportsService service = new SportsService(restClientReturning(board));

        assertThat(service.getScoreboard("not/a-real-league", null)).isEmpty();
    }

    @Test
    void getTeamScheduleReturnsNullWithoutAnyRequestWhenFavoriteTeamIsBlank() {
        SportsService service = new SportsService(mock(RestClient.class));

        assertThat(service.getTeamSchedule("basketball/nba", "  ")).isNull();
        assertThat(service.getTeamSchedule("basketball/nba", null)).isNull();
    }

    @Test
    void getTeamScheduleReturnsNullWhenTeamNameDoesNotMatchAnyEspnTeam() {
        EspnTeamsResponseDto teams = teamsResponseWith("2", "Boston Celtics", "BOS");
        SportsService service = new SportsService(restClientForTeamSchedule(teams, null));

        assertThat(service.getTeamSchedule("basketball/nba", "Nonexistent Team")).isNull();
    }

    @Test
    void getTeamScheduleReturnsMostRecentCompletedGameAndNextScheduledGame() {
        EspnTeamsResponseDto teams = teamsResponseWith("2", "Boston Celtics", "BOS");
        EspnScheduleResponseDto schedule = new EspnScheduleResponseDto();
        schedule.setEvents(List.of(
                scheduleEvent("CAR @ ATL", "2026-09-13T17:00Z", "Boston Celtics", "110", "Charlotte Hornets", "101", true),
                scheduleEvent("BOS @ CLE", "2026-09-20T17:00Z", "Cleveland Cavaliers", "99", "Boston Celtics", "95", true),
                scheduleEvent("PHI @ BOS", "2026-10-11T00:00Z", "Boston Celtics", "0", "Philadelphia 76ers", "0", false),
                scheduleEvent("CHA @ BOS", "2026-10-14T23:30Z", "Boston Celtics", "0", "Charlotte Hornets", "0", false)
        ));
        SportsService service = new SportsService(restClientForTeamSchedule(teams, schedule));

        TeamScheduleDto result = service.getTeamSchedule("basketball/nba", "Celtics");

        assertThat(result.getPreviousGame()).isNotNull();
        assertThat(result.getPreviousGame().getShortName()).isEqualTo("BOS @ CLE");
        assertThat(result.getPreviousGame().isFavorite()).isTrue();
        assertThat(result.getNextGame()).isNotNull();
        assertThat(result.getNextGame().getShortName()).isEqualTo("PHI @ BOS");
        assertThat(result.getNextGame().isFavorite()).isTrue();
    }

    @Test
    void getTeamScheduleLeavesBothGamesNullWhenScheduleHasNoEvents() {
        EspnTeamsResponseDto teams = teamsResponseWith("2", "Boston Celtics", "BOS");
        EspnScheduleResponseDto schedule = new EspnScheduleResponseDto();
        schedule.setEvents(List.of());
        SportsService service = new SportsService(restClientForTeamSchedule(teams, schedule));

        TeamScheduleDto result = service.getTeamSchedule("basketball/nba", "Celtics");

        assertThat(result).isNotNull();
        assertThat(result.getPreviousGame()).isNull();
        assertThat(result.getNextGame()).isNull();
    }
}
