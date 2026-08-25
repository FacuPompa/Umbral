package com.umbral.web.controller;

import com.umbral.domain.entity.Checkpoint;
import com.umbral.domain.entity.Game;
import com.umbral.domain.repository.CheckpointRepository;
import com.umbral.domain.repository.GameRepository;
import com.umbral.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@Transactional
class GameProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CheckpointRepository checkpointRepository;

    @Test
    void updatesCurrentUserProgress() throws Exception {
        Game persona5Royal = gameRepository.findAll().stream()
                .filter(game -> game.getTitle().equals("Persona 5 Royal"))
                .findFirst()
                .orElseThrow();
        List<Checkpoint> checkpoints =
                checkpointRepository.findByGameIdOrderByPositionAsc(persona5Royal.getId());
        Checkpoint kamoshida = checkpoints.get(1);

        mockMvc.perform(put("/api/me/games/{gameId}/progress", persona5Royal.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "checkpointId": %d
                                }
                                """.formatted(kamoshida.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(persona5Royal.getId()))
                .andExpect(jsonPath("$.checkpointId").value(kamoshida.getId()))
                .andExpect(jsonPath("$.checkpointLabel").value("Palacio de Kamoshida"))
                .andExpect(jsonPath("$.position").value(2));
    }

    @Test
    void rejectsNonPositiveCheckpointId() throws Exception {
        mockMvc.perform(put("/api/me/games/{gameId}/progress", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "checkpointId": 0
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundWhenGameDoesNotExist() throws Exception {
        mockMvc.perform(put("/api/me/games/{gameId}/progress", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "checkpointId": 1
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsNotFoundWhenCheckpointDoesNotExist() throws Exception {
        Game persona5Royal = gameRepository.findAll().stream()
                .filter(game -> game.getTitle().equals("Persona 5 Royal"))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(put("/api/me/games/{gameId}/progress", persona5Royal.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "checkpointId": 999999
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsConflictWhenCheckpointBelongsToAnotherGame() throws Exception {
        Game persona5Royal = gameRepository.findAll().stream()
                .filter(game -> game.getTitle().equals("Persona 5 Royal"))
                .findFirst()
                .orElseThrow();
        Game anotherGame = gameRepository.save(new Game(
                "Juego de prueba",
                "Juego creado solo para verificar la pertenencia de checkpoints."
        ));
        Checkpoint anotherGameCheckpoint = checkpointRepository.save(
                new Checkpoint(anotherGame, "Checkpoint incompatible", 1)
        );

        mockMvc.perform(put("/api/me/games/{gameId}/progress", persona5Royal.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "checkpointId": %d
                                }
                                """.formatted(anotherGameCheckpoint.getId())))
                .andExpect(status().isConflict());
    }

    @Test
    void returnsSavedProgressForCurrentUser() throws Exception {
        Game persona5Royal = gameRepository.findAll().stream()
                .filter(game -> game.getTitle().equals("Persona 5 Royal"))
                .findFirst()
                .orElseThrow();
        Checkpoint kamoshida = checkpointRepository
                .findByGameIdOrderByPositionAsc(persona5Royal.getId())
                .get(1);

        mockMvc.perform(put("/api/me/games/{gameId}/progress", persona5Royal.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "checkpointId": %d
                        }
                        """.formatted(kamoshida.getId())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/me/game-progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].gameId").value(persona5Royal.getId()))
                .andExpect(jsonPath("$[0].checkpointId").value(kamoshida.getId()))
                .andExpect(jsonPath("$[0].checkpointLabel").value("Palacio de Kamoshida"))
                .andExpect(jsonPath("$[0].position").value(2));
    }
}
