package com.umbral.web.controller;

import com.umbral.domain.entity.Game;
import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserGameLibrary;
import com.umbral.domain.entity.UserGameLibraryStatus;
import com.umbral.domain.entity.UserRole;
import com.umbral.domain.repository.GameRepository;
import com.umbral.domain.repository.UserGameLibraryRepository;
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
class PublicProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGameLibraryRepository userGameLibraryRepository;

    @Autowired
    private GameRepository gameRepository;

    @Test
    void exposesAPublicProfileWithoutAuthentication() throws Exception {
        User user = userRepository.save(new User(
                "public-controller-test",
                "public-controller-test@umbral.local",
                "not-a-real-password-hash",
                UserRole.MEMBER
        ));
        UserGameLibrary favorite = new UserGameLibrary(user, findPersona5Royal(), UserGameLibraryStatus.COMPLETED);
        favorite.updateFavorite(true);
        userGameLibraryRepository.save(favorite);

        mockMvc.perform(get("/api/users/{handle}", user.getHandle()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.handle").value(user.getHandle()))
                .andExpect(jsonPath("$.libraryCount").value(1))
                .andExpect(jsonPath("$.completedCount").value(1))
                .andExpect(jsonPath("$.favoriteCount").value(1))
                .andExpect(jsonPath("$.favoriteGames[0].gameTitle").value("Persona 5 Royal"))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.role").doesNotExist());
    }

    @Test
    void returnsNotFoundForAnUnknownProfile() throws Exception {
        mockMvc.perform(get("/api/users/missing-profile"))
                .andExpect(status().isNotFound());
    }

    private Game findPersona5Royal() {
        return gameRepository.findAll().stream()
                .filter(game -> game.getTitle().equals("Persona 5 Royal"))
                .findFirst()
                .orElseThrow();
    }
}
