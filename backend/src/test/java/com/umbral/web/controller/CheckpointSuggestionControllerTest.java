package com.umbral.web.controller;

import com.umbral.domain.entity.CheckpointSuggestion;
import com.umbral.domain.entity.Game;
import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserRole;
import com.umbral.domain.repository.CheckpointRepository;
import com.umbral.domain.repository.CheckpointSuggestionRepository;
import com.umbral.domain.repository.GameRepository;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@Transactional
class CheckpointSuggestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CheckpointSuggestionRepository checkpointSuggestionRepository;

    @Autowired
    private CheckpointRepository checkpointRepository;

    @Test
    void createsCheckpointSuggestionForTheAuthenticatedUser() throws Exception {
        Game game = persona5Royal();

        mockMvc.perform(post("/api/games/{gameId}/checkpoint-suggestions", game.getId())
                        .with(user("umbral-demo"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "label": "Tramo de prueba",
                                  "position": 11
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameId").value(game.getId()))
                .andExpect(jsonPath("$.label").value("Tramo de prueba"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void allowsOnlyModeratorsToReviewCheckpointSuggestions() throws Exception {
        Game game = persona5Royal();

        mockMvc.perform(get("/api/moderation/checkpoint-suggestions")
                        .with(user("umbral-demo")))
                .andExpect(status().isForbidden());

        User moderator = userRepository.save(new User(
                "moderator",
                "moderator@example.com",
                "not-used-in-this-test",
                UserRole.MODERATOR
        ));
        User author = userRepository.findByHandle("umbral-demo").orElseThrow();
        CheckpointSuggestion suggestion = checkpointSuggestionRepository.save(
                new CheckpointSuggestion(game, "Tramo de prueba", 11, author)
        );

        mockMvc.perform(post("/api/moderation/checkpoint-suggestions/{suggestionId}/approve", suggestion.getId())
                        .with(user(moderator.getHandle()).roles("MODERATOR"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(get("/api/games/{gameId}/checkpoints", game.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[10].label").value("Tramo de prueba"));
    }

    private Game persona5Royal() {
        return gameRepository.findAll().stream()
                .filter(candidate -> candidate.getTitle().equals("Persona 5 Royal"))
                .findFirst()
                .orElseThrow();
    }
}
