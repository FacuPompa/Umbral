package com.umbral.domain.repository;

import com.umbral.domain.entity.CheckpointSuggestion;
import com.umbral.domain.entity.CheckpointSuggestionStatus;
import com.umbral.domain.entity.Game;
import com.umbral.domain.entity.User;
import com.umbral.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@Transactional
class CheckpointSuggestionRepositoryTest {

    @Autowired
    private CheckpointSuggestionRepository checkpointSuggestionRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findsPendingSuggestionsInCreationOrder() {
        Game game = gameRepository.findAll().stream()
                .filter(candidate -> candidate.getTitle().equals("Persona 5 Royal"))
                .findFirst()
                .orElseThrow();
        User author = userRepository.findByHandle("umbral-demo").orElseThrow();

        CheckpointSuggestion suggestion = checkpointSuggestionRepository.save(
                new CheckpointSuggestion(game, "Tramo de prueba", 11, author)
        );

        List<CheckpointSuggestion> pendingSuggestions = checkpointSuggestionRepository
                .findAllByStatusOrderByCreatedAtAscIdAsc(CheckpointSuggestionStatus.PENDING);

        assertEquals(suggestion.getId(), pendingSuggestions.getLast().getId());
        assertTrue(checkpointSuggestionRepository.existsByGameIdAndPositionAndStatus(
                game.getId(),
                11,
                CheckpointSuggestionStatus.PENDING
        ));
    }
}
