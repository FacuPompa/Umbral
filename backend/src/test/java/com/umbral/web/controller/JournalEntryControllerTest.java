package com.umbral.web.controller;

import com.umbral.domain.entity.Checkpoint;
import com.umbral.domain.entity.Game;
import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserGameProgress;
import com.umbral.domain.repository.CheckpointRepository;
import com.umbral.domain.repository.GameRepository;
import com.umbral.domain.repository.UserGameProgressRepository;
import com.umbral.domain.repository.UserRepository;
import com.umbral.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@Transactional
@WithMockUser(username = "umbral-demo")
class JournalEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CheckpointRepository checkpointRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGameProgressRepository userGameProgressRepository;

    @Test
    void returnsOnlyEntriesAllowedByCurrentUserProgress() throws Exception {
        Game persona5Royal = findPersona5Royal();
        Checkpoint madarame = checkpointRepository
                .findByGameIdOrderByPositionAsc(persona5Royal.getId())
                .get(2);

        saveProgressForDemoUser(persona5Royal, madarame);

        mockMvc.perform(get("/api/games/{gameId}/journal-entries", persona5Royal.getId())
                        .with(user("umbral-demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.items[0].checkpointLabel").value("Palacio de Madarame"))
                .andExpect(jsonPath("$.items[0].type").value("REFLECTION"));
    }

    @Test
    void createsJournalEntryWhenCurrentUserReachedTheCheckpoint() throws Exception {
        Game persona5Royal = findPersona5Royal();
        Checkpoint madarame = checkpointRepository
                .findByGameIdOrderByPositionAsc(persona5Royal.getId())
                .get(2);

        saveProgressForDemoUser(persona5Royal, madarame);

        mockMvc.perform(post("/api/me/journal-entries")
                        .with(user("umbral-demo"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "checkpointId": %d,
                                  "type": "QUESTION",
                                  "content": "Una entrada creada a través del controller."
                                }
                                """.formatted(madarame.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorHandle").value("umbral-demo"))
                .andExpect(jsonPath("$.gameId").value(persona5Royal.getId()))
                .andExpect(jsonPath("$.checkpointLabel").value("Palacio de Madarame"))
                .andExpect(jsonPath("$.type").value("QUESTION"))
                .andExpect(jsonPath("$.content")
                        .value("Una entrada creada a través del controller."));
    }

    @Test
    void rejectsEntryBeyondCurrentUserProgress() throws Exception {
        Game persona5Royal = findPersona5Royal();
        Checkpoint madarame = checkpointRepository
                .findByGameIdOrderByPositionAsc(persona5Royal.getId())
                .get(2);
        Checkpoint niijima = checkpointRepository
                .findByGameIdOrderByPositionAsc(persona5Royal.getId())
                .get(6);

        saveProgressForDemoUser(persona5Royal, madarame);

        mockMvc.perform(post("/api/me/journal-entries")
                        .with(user("umbral-demo"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "checkpointId": %d,
                                  "type": "THEORY",
                                  "content": "Esta entrada debería ser rechazada."
                                }
                                """.formatted(niijima.getId())))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsBlankContent() throws Exception {
        mockMvc.perform(post("/api/me/journal-entries")
                        .with(user("umbral-demo"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "checkpointId": 1,
                                  "type": "REFLECTION",
                                  "content": " "
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsEntryWithoutType() throws Exception {
        mockMvc.perform(post("/api/me/journal-entries")
                        .with(user("umbral-demo"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "checkpointId": 1,
                                  "content": "Una publicación sin tipo."
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    private Game findPersona5Royal() {
        return gameRepository.findAll().stream()
                .filter(game -> game.getTitle().equals("Persona 5 Royal"))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void supportsTypeFilteringAndAnEmptyFilteredFeed() throws Exception {
        Game game = findPersona5Royal();
        saveProgressForDemoUser(game, checkpointRepository.findByGameIdOrderByPositionAsc(game.getId()).get(6));
        mockMvc.perform(get("/api/games/{id}/journal-entries", game.getId()).with(user("umbral-demo"))
                        .param("type", "THEORY").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].type").value("THEORY"))
                .andExpect(jsonPath("$.hasNext").value(false));
        mockMvc.perform(get("/api/games/{id}/journal-entries", game.getId()).with(user("umbral-demo")).param("type", "QUESTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void supportsAscendingOrderAndSubsequentPages() throws Exception {
        Game game = findPersona5Royal();
        saveProgressForDemoUser(game, checkpointRepository.findByGameIdOrderByPositionAsc(game.getId()).get(6));
        mockMvc.perform(get("/api/games/{id}/journal-entries", game.getId()).with(user("umbral-demo")).param("order", "ASC").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].checkpointLabel").value("Palacio de Madarame"))
                .andExpect(jsonPath("$.hasNext").value(true));
        mockMvc.perform(get("/api/games/{id}/journal-entries", game.getId()).with(user("umbral-demo"))
                        .param("order", "ASC").param("size", "1").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].type").value("THEORY"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void rejectsInvalidFeedParameters() throws Exception {
        Long gameId = findPersona5Royal().getId();
        String[][] invalid = {{"page", "-1"}, {"page", "10001"}, {"size", "0"}, {"size", "51"},
                {"type", "INVALID"}, {"order", "INVALID"}, {"page", "abc"}};
        for (String[] parameter : invalid) {
            mockMvc.perform(get("/api/games/{id}/journal-entries", gameId).with(user("umbral-demo")).param(parameter[0], parameter[1]))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void returnsEmptyPageWithoutProgressAndRequiresAuthentication() throws Exception {
        Long gameId = findPersona5Royal().getId();
        mockMvc.perform(get("/api/games/{id}/journal-entries", gameId).with(user("umbral-demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.hasNext").value(false));
        mockMvc.perform(get("/api/games/{id}/journal-entries", gameId).with(anonymous()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/games/{id}/journal-entries", Long.MAX_VALUE).with(user("umbral-demo")))
                .andExpect(status().isNotFound());
    }

    private void saveProgressForDemoUser(Game game, Checkpoint checkpoint) {
        User demoUser = userRepository.findById(1L)
                .orElseThrow();

        userGameProgressRepository.save(
                new UserGameProgress(demoUser, game, checkpoint)
        );
    }
}
