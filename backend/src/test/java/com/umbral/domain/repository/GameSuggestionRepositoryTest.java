package com.umbral.domain.repository;

import com.umbral.domain.entity.GameSuggestion;
import com.umbral.domain.entity.GameSuggestionStatus;
import com.umbral.domain.entity.User;
import com.umbral.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@Transactional
class GameSuggestionRepositoryTest {

    @Autowired
    private GameSuggestionRepository gameSuggestionRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void returnsPendingSuggestionsInCreationOrder() {
        User author = userRepository.findById(1L).orElseThrow();
        gameSuggestionRepository.save(new GameSuggestion(
                3498L,
                "Persona 5 Royal",
                LocalDate.of(2020, 3, 31),
                "https://images.example.test/persona-5-royal.jpg",
                author
        ));
        gameSuggestionRepository.save(new GameSuggestion(
                9999L,
                "Persona 3 Reload",
                LocalDate.of(2024, 2, 2),
                "https://images.example.test/persona-3-reload.jpg",
                author
        ));

        List<GameSuggestion> pendingSuggestions = gameSuggestionRepository
                .findAllByStatusOrderByCreatedAtAscIdAsc(GameSuggestionStatus.PENDING);

        assertEquals(2, pendingSuggestions.size());
        assertEquals("Persona 5 Royal", pendingSuggestions.get(0).getTitle());
        assertEquals("Persona 3 Reload", pendingSuggestions.get(1).getTitle());
    }
}
