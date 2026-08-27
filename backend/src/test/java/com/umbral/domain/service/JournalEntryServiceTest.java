package com.umbral.domain.service;

import com.umbral.domain.dto.CreateJournalEntryRequest;
import com.umbral.domain.dto.JournalEntryResponse;
import com.umbral.domain.entity.Checkpoint;
import com.umbral.domain.entity.Game;
import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserGameProgress;
import com.umbral.domain.exception.AuthorCannotPublishBeyondProgressException;
import com.umbral.domain.repository.CheckpointRepository;
import com.umbral.domain.repository.GameRepository;
import com.umbral.domain.repository.UserGameProgressRepository;
import com.umbral.domain.repository.UserRepository;
import com.umbral.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@Transactional
class JournalEntryServiceTest {

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CheckpointRepository checkpointRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGameProgressRepository userGameProgressRepository;

    @Test
    void createsEntryWhenAuthorReachedTheCheckpoint() {
        Game persona5Royal = findPersona5Royal();
        Checkpoint madarame = checkpointRepository
                .findByGameIdOrderByPositionAsc(persona5Royal.getId())
                .get(2);

        saveProgressForDemoUser(persona5Royal, madarame);

        JournalEntryResponse response = journalEntryService
                .createCurrentUserEntry(new CreateJournalEntryRequest(
                        madarame.getId(),
                        "Una entrada creada desde un test."
                ));

        assertNotNull(response.id());
        assertEquals("umbral-demo", response.authorHandle());
        assertEquals(persona5Royal.getId(), response.gameId());
        assertEquals("Palacio de Madarame", response.checkpointLabel());
    }

    @Test
    void rejectsEntryWhenAuthorDidNotReachTheCheckpoint() {
        Game persona5Royal = findPersona5Royal();
        Checkpoint madarame = checkpointRepository
                .findByGameIdOrderByPositionAsc(persona5Royal.getId())
                .get(2);
        Checkpoint niijima = checkpointRepository
                .findByGameIdOrderByPositionAsc(persona5Royal.getId())
                .get(6);

        saveProgressForDemoUser(persona5Royal, madarame);

        assertThrows(
                AuthorCannotPublishBeyondProgressException.class,
                () -> journalEntryService.createCurrentUserEntry(
                        new CreateJournalEntryRequest(
                                niijima.getId(),
                                "Esta entrada no debería publicarse."
                        )
                )
        );
    }

    private Game findPersona5Royal() {
        return gameRepository.findAll().stream()
                .filter(game -> game.getTitle().equals("Persona 5 Royal"))
                .findFirst()
                .orElseThrow();
    }

    private void saveProgressForDemoUser(Game game, Checkpoint checkpoint) {
        User demoUser = userRepository.findById(1L)
                .orElseThrow();

        userGameProgressRepository.save(
                new UserGameProgress(demoUser, game, checkpoint)
        );
    }
}