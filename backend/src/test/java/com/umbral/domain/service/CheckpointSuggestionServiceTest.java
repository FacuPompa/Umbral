package com.umbral.domain.service;

import com.umbral.domain.dto.CheckpointSuggestionResponse;
import com.umbral.domain.dto.CreateCheckpointSuggestionRequest;
import com.umbral.domain.entity.CheckpointSuggestion;
import com.umbral.domain.entity.CheckpointSuggestionStatus;
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
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@Transactional
class CheckpointSuggestionServiceTest {

    @Autowired
    private CheckpointSuggestionService checkpointSuggestionService;

    @Autowired
    private CheckpointSuggestionRepository checkpointSuggestionRepository;

    @Autowired
    private CheckpointRepository checkpointRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "umbral-demo")
    void createsAPendingCheckpointSuggestionForTheCurrentUser() {
        Game game = persona5Royal();

        CheckpointSuggestionResponse suggestion = checkpointSuggestionService.createCurrentUserSuggestion(
                game.getId(),
                new CreateCheckpointSuggestionRequest("Tramo de prueba", 11)
        );

        assertEquals("Tramo de prueba", suggestion.label());
        assertEquals(CheckpointSuggestionStatus.PENDING, suggestion.status());
        assertEquals("umbral-demo", suggestion.suggestedByHandle());
    }

    @Test
    @WithMockUser(username = "moderator")
    void approvesSuggestionAndCreatesTheRealCheckpoint() {
        User moderator = userRepository.save(new User(
                "moderator",
                "moderator@example.com",
                "not-used-in-this-test",
                UserRole.MODERATOR
        ));
        Game game = persona5Royal();
        User author = userRepository.findByHandle("umbral-demo").orElseThrow();
        CheckpointSuggestion suggestion = checkpointSuggestionRepository.save(
                new CheckpointSuggestion(game, "Tramo de prueba", 11, author)
        );

        CheckpointSuggestionResponse approved = checkpointSuggestionService.approveSuggestion(suggestion.getId());

        assertEquals(CheckpointSuggestionStatus.APPROVED, approved.status());
        assertEquals("moderator", moderator.getHandle());
        assertTrue(checkpointRepository.existsByGameIdAndPosition(game.getId(), 11));
        assertEquals(0, checkpointSuggestionService.getPendingSuggestionsForModerator().size());
    }

    private Game persona5Royal() {
        return gameRepository.findAll().stream()
                .filter(candidate -> candidate.getTitle().equals("Persona 5 Royal"))
                .findFirst()
                .orElseThrow();
    }
}
