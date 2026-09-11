package com.umbral.web.controller;

import com.umbral.domain.entity.Game;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@Transactional
class UserGameLibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameRepository gameRepository;

    @Test
    void addsAndListsAGameInTheCurrentUsersLibrary() throws Exception {
        Game persona5Royal = findPersona5Royal();

        mockMvc.perform(post("/api/me/library")
                        .with(user("umbral-demo"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gameId": %d,
                                  "status": "WANT_TO_PLAY"
                                }
                                """.formatted(persona5Royal.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameId").value(persona5Royal.getId()))
                .andExpect(jsonPath("$.status").value("WANT_TO_PLAY"))
                .andExpect(jsonPath("$.favorite").value(false));

        mockMvc.perform(get("/api/me/library")
                        .with(user("umbral-demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].gameId").value(persona5Royal.getId()))
                .andExpect(jsonPath("$[0].checkpointLabel").doesNotExist());
    }

    @Test
    void updatesAStatusAndFavoriteInTheCurrentUsersLibrary() throws Exception {
        Game persona5Royal = findPersona5Royal();
        addPersona5RoyalToDemoLibrary("PLAYING");

        mockMvc.perform(patch("/api/me/library/{gameId}/status", persona5Royal.getId())
                        .with(user("umbral-demo"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "COMPLETED" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(patch("/api/me/library/{gameId}/favorite", persona5Royal.getId())
                        .with(user("umbral-demo"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "favorite": true }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite").value(true));
    }

    @Test
    void removesAGameFromTheCurrentUsersLibrary() throws Exception {
        Game persona5Royal = findPersona5Royal();
        addPersona5RoyalToDemoLibrary("PLAYING");

        mockMvc.perform(delete("/api/me/library/{gameId}", persona5Royal.getId())
                        .with(user("umbral-demo"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/me/library")
                        .with(user("umbral-demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void rejectsAddingAGameWithoutAStatus() throws Exception {
        mockMvc.perform(post("/api/me/library")
                        .with(user("umbral-demo"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "gameId": 1 }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsAddingTheSameGameTwice() throws Exception {
        addPersona5RoyalToDemoLibrary("PLAYING");

        mockMvc.perform(post("/api/me/library")
                        .with(user("umbral-demo"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "gameId": %d, "status": "PLAYING" }
                                """.formatted(findPersona5Royal().getId())))
                .andExpect(status().isConflict());
    }

    private void addPersona5RoyalToDemoLibrary(String status) throws Exception {
        mockMvc.perform(post("/api/me/library")
                        .with(user("umbral-demo"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "gameId": %d, "status": "%s" }
                                """.formatted(findPersona5Royal().getId(), status)))
                .andExpect(status().isCreated());
    }

    private Game findPersona5Royal() {
        return gameRepository.findAll().stream()
                .filter(game -> game.getTitle().equals("Persona 5 Royal"))
                .findFirst()
                .orElseThrow();
    }
}
