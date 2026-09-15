package com.umbral.web.controller;

import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserRole;
import com.umbral.domain.repository.UserRepository;
import com.umbral.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@Transactional
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void searchesGamesAndPublicProfilesWithoutAuthentication() throws Exception {
        userRepository.save(new User(
                "persona-fan",
                "persona-fan@umbral.local",
                "not-a-real-password-hash",
                UserRole.MEMBER
        ));

        mockMvc.perform(get("/api/search").param("query", "persona"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.games[0].title").value("Persona 5 Royal"))
                .andExpect(jsonPath("$.users[0].handle").value("persona-fan"))
                .andExpect(jsonPath("$.users[0].email").doesNotExist())
                .andExpect(jsonPath("$.users[0].role").doesNotExist());
    }

    @Test
    void returnsEmptyListsForAShortQuery() throws Exception {
        mockMvc.perform(get("/api/search").param("query", "p"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.games").isEmpty())
                .andExpect(jsonPath("$.users").isEmpty());
    }
}
