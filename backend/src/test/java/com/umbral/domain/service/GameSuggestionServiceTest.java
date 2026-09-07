package com.umbral.domain.service;

import com.umbral.domain.dto.ApproveGameSuggestionRequest;
import com.umbral.domain.dto.CreateGameSuggestionRequest;
import com.umbral.domain.dto.GameSuggestionResponse;
import com.umbral.domain.entity.GameSuggestion;
import com.umbral.domain.entity.GameSuggestionStatus;
import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserRole;
import com.umbral.domain.exception.GameAlreadyInCatalogException;
import com.umbral.domain.repository.GameRepository;
import com.umbral.domain.repository.GameSuggestionRepository;
import com.umbral.domain.repository.UserRepository;
import com.umbral.support.FakeGameDiscoveryConfiguration;
import com.umbral.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import({PostgresTestConfiguration.class, FakeGameDiscoveryConfiguration.class})
@Transactional
class GameSuggestionServiceTest {

    @Autowired
    private GameSuggestionService gameSuggestionService;

    @Autowired
    private GameSuggestionRepository gameSuggestionRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "umbral-demo")
    void createsPendingSuggestionFromTheSelectedExternalGame() {
        GameSuggestionResponse suggestion = gameSuggestionService.createCurrentUserSuggestion(
                new CreateGameSuggestionRequest(9999L)
        );

        assertEquals("Persona 3 Reload", suggestion.title());
        assertEquals(GameSuggestionStatus.PENDING, suggestion.status());
        assertEquals("umbral-demo", suggestion.suggestedByHandle());
        assertTrue(gameSuggestionRepository.existsByRawgGameId(9999L));
    }

    @Test
    @WithMockUser(username = "umbral-demo")
    void rejectsSuggestionsForAGameAlreadyInTheCatalog() {
        assertThrows(
                GameAlreadyInCatalogException.class,
                () -> gameSuggestionService.createCurrentUserSuggestion(
                        new CreateGameSuggestionRequest(3498L)
                )
        );
    }

    @Test
    @WithMockUser(username = "moderator")
    void approvesSuggestionAndCreatesTheLocalGame() {
        User author = userRepository.findById(1L).orElseThrow();
        userRepository.save(new User(
                "moderator",
                "moderator@example.com",
                "not-used-in-this-test",
                UserRole.MODERATOR
        ));
        GameSuggestion pendingSuggestion = gameSuggestionRepository.save(new GameSuggestion(
                9999L,
                "Persona 3 Reload",
                LocalDate.of(2024, 2, 2),
                "https://images.example.test/persona-3-reload.jpg",
                author
        ));

        GameSuggestionResponse approvedSuggestion = gameSuggestionService.approveSuggestion(
                pendingSuggestion.getId(),
                new ApproveGameSuggestionRequest("Una nueva versión de Persona 3 para explorar.")
        );

        assertEquals(GameSuggestionStatus.APPROVED, approvedSuggestion.status());
        assertTrue(gameRepository.existsByRawgGameId(9999L));
        assertEquals(0, gameSuggestionService.getPendingSuggestionsForModerator().size());
    }
}
