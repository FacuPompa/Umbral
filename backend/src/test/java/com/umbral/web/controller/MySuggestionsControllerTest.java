package com.umbral.web.controller;

import com.umbral.domain.entity.*;
import com.umbral.domain.repository.*;
import com.umbral.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@Transactional
class MySuggestionsControllerTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired GameRepository games;
    @Autowired GameSuggestionRepository gameSuggestions;
    @Autowired CheckpointSuggestionRepository checkpointSuggestions;
    @Autowired JdbcTemplate jdbc;

    private User author() { return users.findByHandle("umbral-demo").orElseThrow(); }
    private User other() { return users.findByHandle("umbral-author-demo").orElseThrow(); }
    private Game game() { return games.findAll().getFirst(); }

    private GameSuggestion suggest(long rawgId, User author) {
        return gameSuggestions.save(new GameSuggestion(rawgId, "Juego " + rawgId, null, null, author));
    }

    @ParameterizedTest
    @ValueSource(strings = {"games", "checkpoints"})
    void requiresAuthentication(String kind) throws Exception {
        mvc.perform(get("/api/me/suggestions/" + kind)).andExpect(status().isUnauthorized());
    }

    @Test
    void returnsOnlyOwnGamesInStableNewestFirstPages() throws Exception {
        var first = suggest(90001, author());
        var second = suggest(90002, author());
        suggest(90003, other());
        // Equal dates must still have a deterministic id ordering.
        gameSuggestions.flush();
        jdbc.update("update game_suggestions set created_at = (select created_at from game_suggestions where id = ?) where id = ?",
                first.getId(), second.getId());
        mvc.perform(get("/api/me/suggestions/games").with(user(author().getHandle())).param("size", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(second.getId()))
                .andExpect(jsonPath("$.page").value(0)).andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.items[0].email").doesNotExist())
                .andExpect(jsonPath("$.items[0].reviewedBy").doesNotExist());
        mvc.perform(get("/api/me/suggestions/games").with(user(author().getHandle())).param("size", "1").param("page", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].id").value(first.getId()))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PENDING", "APPROVED", "REJECTED"})
    void filtersOwnGamesByPersistedReviewStatus(String filter) throws Exception {
        var pending = suggest(90011, author());
        var approved = suggest(90012, author());
        var rejected = suggest(90013, author());
        approved.approve(other());
        rejected.reject(other());
        var foreign = suggest(90014, other());
        foreign.approve(other());
        Long expected = switch (filter) {
            case "APPROVED" -> approved.getId();
            case "REJECTED" -> rejected.getId();
            default -> pending.getId();
        };
        mvc.perform(get("/api/me/suggestions/games").with(user(author().getHandle())).param("status", filter))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(expected))
                .andExpect(jsonPath("$.items[0].status").value(filter));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PENDING", "APPROVED", "REJECTED"})
    void filtersOwnCheckpointsWithoutRevealingOtherUsersProposals(String filter) throws Exception {
        var pending = checkpointSuggestions.save(new CheckpointSuggestion(game(), "Mi propuesta", 101, author()));
        var approved = checkpointSuggestions.save(new CheckpointSuggestion(game(), "Aprobada", 102, author()));
        var rejected = checkpointSuggestions.save(new CheckpointSuggestion(game(), "Rechazada", 103, author()));
        approved.approve(other());
        rejected.reject(other());
        checkpointSuggestions.save(new CheckpointSuggestion(game(), "Propuesta ajena", 104, other()));
        Long expected = switch (filter) {
            case "APPROVED" -> approved.getId();
            case "REJECTED" -> rejected.getId();
            default -> pending.getId();
        };
        mvc.perform(get("/api/me/suggestions/checkpoints").with(user(author().getHandle())).param("status", filter))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(expected))
                .andExpect(jsonPath("$.items[0].status").value(filter))
                .andExpect(jsonPath("$.items[0].gameId").value(game().getId()));
    }

    @Test
    void paginatesCheckpointsBeforeReturningResults() throws Exception {
        var first = checkpointSuggestions.save(new CheckpointSuggestion(game(), "Primera", 101, author()));
        var second = checkpointSuggestions.save(new CheckpointSuggestion(game(), "Segunda", 102, author()));
        checkpointSuggestions.save(new CheckpointSuggestion(game(), "Ajena", 103, other()));
        mvc.perform(get("/api/me/suggestions/checkpoints").with(user(author().getHandle())).param("size", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].id").value(second.getId()))
                .andExpect(jsonPath("$.hasNext").value(true));
        mvc.perform(get("/api/me/suggestions/checkpoints").with(user(author().getHandle())).param("size", "1").param("page", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].id").value(first.getId()))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"games", "checkpoints"})
    void returnsAnEmptyPageAndIgnoresClientSuppliedIdentity(String kind) throws Exception {
        suggest(90100, other());
        checkpointSuggestions.save(new CheckpointSuggestion(game(), "Ajena", 101, other()));
        mvc.perform(get("/api/me/suggestions/" + kind).with(user(author().getHandle()))
                        .param("userId", other().getId().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"games", "checkpoints"})
    void validatesPaginationAndStatus(String kind) throws Exception {
        for (String invalid : new String[]{"page=-1", "page=10001", "size=0", "size=51", "status=UNKNOWN"}) {
            String[] param = invalid.split("=");
            mvc.perform(get("/api/me/suggestions/" + kind).with(user(author().getHandle())).param(param[0], param[1]))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void evenModeratorsSeeOnlyTheirOwnHistory() throws Exception {
        var moderator = users.save(new User("history-mod", "history-mod@example.test", "unused", UserRole.MODERATOR));
        suggest(90200, author());
        suggest(90201, moderator);
        mvc.perform(get("/api/me/suggestions/games").with(user(moderator.getHandle()).roles("MODERATOR")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].suggestedByHandle").value(moderator.getHandle()));
        assertEquals(2, gameSuggestions.count());
    }
}
