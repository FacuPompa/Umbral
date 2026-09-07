package com.umbral.web.controller;

import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserRole;
import com.umbral.domain.repository.UserRepository;
import com.umbral.support.FakeGameDiscoveryConfiguration;
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
@Import({PostgresTestConfiguration.class, FakeGameDiscoveryConfiguration.class})
@Transactional
class GameSuggestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void searchesExternalGamesForAnAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/game-suggestions/search")
                        .param("query", "persona")
                        .with(user("umbral-demo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rawgGameId").value(3498))
                .andExpect(jsonPath("$[1].title").value("Persona 3 Reload"));
    }

    @Test
    void createsSuggestionForTheCurrentUser() throws Exception {
        mockMvc.perform(post("/api/game-suggestions")
                        .with(user("umbral-demo"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rawgGameId": 9999
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Persona 3 Reload"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void allowsOnlyModeratorsToSeePendingSuggestions() throws Exception {
        mockMvc.perform(get("/api/moderation/game-suggestions")
                        .with(user("umbral-demo")))
                .andExpect(status().isForbidden());

        userRepository.save(new User(
                "moderator",
                "moderator@example.com",
                "not-used-in-this-test",
                UserRole.MODERATOR
        ));

        mockMvc.perform(get("/api/moderation/game-suggestions")
                        .with(user("moderator").roles("MODERATOR")))
                .andExpect(status().isOk());
    }
}
