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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@Transactional
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

        mockMvc.perform(get("/api/games/{gameId}/journal-entries", persona5Royal.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].checkpointLabel").value("Palacio de Madarame"));
    }

    @Test
    void createsJournalEntryWhenCurrentUserReachedTheCheckpoint() throws Exception {
        Game persona5Royal = findPersona5Royal();
        Checkpoint madarame = checkpointRepository
                .findByGameIdOrderByPositionAsc(persona5Royal.getId())
                .get(2);

        saveProgressForDemoUser(persona5Royal, madarame);

        mockMvc.perform(post("/api/me/journal-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "checkpointId": %d,
                                  "content": "Una entrada creada a través del controller."
                                }
                                """.formatted(madarame.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorHandle").value("umbral-demo"))
                .andExpect(jsonPath("$.gameId").value(persona5Royal.getId()))
                .andExpect(jsonPath("$.checkpointLabel").value("Palacio de Madarame"))
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "checkpointId": %d,
                                  "content": "Esta entrada debería ser rechazada."
                                }
                                """.formatted(niijima.getId())))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsBlankContent() throws Exception {
        mockMvc.perform(post("/api/me/journal-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "checkpointId": 1,
                                  "content": " "
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

    private void saveProgressForDemoUser(Game game, Checkpoint checkpoint) {
        User demoUser = userRepository.findById(1L)
                .orElseThrow();

        userGameProgressRepository.save(
                new UserGameProgress(demoUser, game, checkpoint)
        );
    }
}