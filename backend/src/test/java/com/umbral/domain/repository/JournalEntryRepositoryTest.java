package com.umbral.domain.repository;

import com.umbral.domain.entity.Checkpoint;
import com.umbral.domain.entity.Game;
import com.umbral.domain.entity.JournalEntry;
import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserGameProgress;
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
class JournalEntryRepositoryTest {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CheckpointRepository checkpointRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGameProgressRepository userGameProgressRepository;

    @Test
    void returnsEmptyFeedWhenReaderHasNoProgressForTheGame() {
        Game persona5Royal = findPersona5Royal();

        List<JournalEntry> entries = journalEntryRepository
                .findVisibleByReaderIdAndGameId(1L, persona5Royal.getId());

        assertTrue(entries.isEmpty());
    }

    @Test
    void returnsOnlyEntriesAtOrBeforeReaderProgress() {
        Game persona5Royal = findPersona5Royal();
        Checkpoint madarame = checkpointRepository
                .findByGameIdOrderByPositionAsc(persona5Royal.getId())
                .get(2);

        saveProgressForDemoUser(persona5Royal, madarame);

        List<JournalEntry> entries = journalEntryRepository
                .findVisibleByReaderIdAndGameId(1L, persona5Royal.getId());

        assertEquals(1, entries.size());
        assertEquals(3, entries.getFirst().getCheckpoint().getPosition());
    }

    @Test
    void returnsVisibleEntriesOrderedFromNewestToOldest() {
        Game persona5Royal = findPersona5Royal();
        Checkpoint niijima = checkpointRepository
                .findByGameIdOrderByPositionAsc(persona5Royal.getId())
                .get(6);

        saveProgressForDemoUser(persona5Royal, niijima);

        List<JournalEntry> entries = journalEntryRepository
                .findVisibleByReaderIdAndGameId(1L, persona5Royal.getId());

        assertEquals(2, entries.size());
        assertEquals(7, entries.getFirst().getCheckpoint().getPosition());
        assertEquals(3, entries.get(1).getCheckpoint().getPosition());
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