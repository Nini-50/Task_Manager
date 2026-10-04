package com.academictaskmanager.service;

import com.academictaskmanager.dto.EspnCompetitionDto;
import com.academictaskmanager.dto.EspnCompetitorDto;
import com.academictaskmanager.dto.EspnEventDto;
import com.academictaskmanager.dto.EspnScoreboardDto;
import com.academictaskmanager.dto.EspnStatusDto;
import com.academictaskmanager.dto.EspnStatusTypeDto;
import com.academictaskmanager.dto.EspnTeamDto;
import com.academictaskmanager.dto.SportsGameDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SportsServiceTest {

    private EspnEventDto game(String shortName, String homeTeam, String homeScore,
                               String awayTeam, String awayScore) {
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

        EspnCompetitionDto competition = new EspnCompetitionDto();
        competition.setCompetitors(List.of(homeCompetitor, awayCompetitor));

        EspnStatusTypeDto type = new EspnStatusTypeDto();
        type.setDescription("Final");
        type.setCompleted(true);
        EspnStatusDto status = new EspnStatusDto();
        status.setType(type);

        EspnEventDto event = new EspnEventDto();
        event.setShortName(shortName);
        event.setStatus(status);
        event.setCompetitions(List.of(competition));
        return event;
    }

    private RestClient restClientReturning(EspnScoreboardDto board) {
        RestClient restClient = mock(RestClient.class, Answers.RETURNS_DEEP_STUBS);
        when(restClient.get().uri(any(java.net.URI.class)).retrieve().body(EspnScoreboardDto.class))
                .thenReturn(board);
        return restClient;
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
}
