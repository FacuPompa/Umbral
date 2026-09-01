package com.umbral.domain.service;

import com.umbral.domain.dto.CreateJournalReplyRequest;
import com.umbral.domain.dto.JournalReplyResponse;
import com.umbral.domain.entity.Checkpoint;
import com.umbral.domain.entity.Game;
import com.umbral.domain.entity.JournalEntry;
import com.umbral.domain.entity.JournalEntryType;
import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserGameProgress;
import com.umbral.domain.exception.ResourceNotFoundException;
import com.umbral.domain.repository.CheckpointRepository;
import com.umbral.domain.repository.GameRepository;
import com.umbral.domain.repository.JournalEntryRepository;
import com.umbral.domain.repository.JournalReplyRepository;
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
class JournalReplyServiceTest {

    @Autowired
    private JournalReplyService journalReplyService;

    @Autowired
    private JournalReplyRepository journalReplyRepository;

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
    void createsReplyWhenCurrentUserReachedEntryCheckpoint() {
        Game persona5Royal = findPersona5Royal();
        Checkpoint madarame = checkpointAt(persona5Royal, 2);
        saveProgressForDemoUser(persona5Royal, madarame);
        JournalEntry entry = saveEntry(authorDemoUser(), madarame);

        JournalReplyResponse response = journalReplyService.createCurrentUserReply(
                entry.getId(),
                new CreateJournalReplyRequest("Una respuesta creada desde un test.")
        );

        assertNotNull(response.id());
        assertEquals("umbral-demo", response.authorHandle());
        assertEquals("Una respuesta creada desde un test.", response.content());
        assertEquals(
                1,
                journalReplyRepository
                        .findByJournalEntryIdOrderByCreatedAtAscIdAsc(entry.getId())
                        .size()
        );
    }

    @Test
    void hidesEntryRepliesWhenCurrentUserDidNotReachEntryCheckpoint() {
        Game persona5Royal = findPersona5Royal();
        Checkpoint kamoshida = checkpointAt(persona5Royal, 1);
        Checkpoint madarame = checkpointAt(persona5Royal, 2);
        saveProgressForDemoUser(persona5Royal, kamoshida);
        JournalEntry entry = saveEntry(authorDemoUser(), madarame);

        assertThrows(
                ResourceNotFoundException.class,
                () -> journalReplyService.getVisibleRepliesForCurrentUser(entry.getId())
        );
    }

    private Game findPersona5Royal() {
        return gameRepository.findAll().stream()
                .filter(game -> game.getTitle().equals("Persona 5 Royal"))
                .findFirst()
                .orElseThrow();
    }

    private Checkpoint checkpointAt(Game game, int index) {
        return checkpointRepository
                .findByGameIdOrderByPositionAsc(game.getId())
                .get(index);
    }

    private User demoUser() {
        return userRepository.findById(1L)
                .orElseThrow();
    }

    private User authorDemoUser() {
        return userRepository.findAll().stream()
                .filter(user -> user.getHandle().equals("umbral-author-demo"))
                .findFirst()
                .orElseThrow();
    }

    private void saveProgressForDemoUser(Game game, Checkpoint checkpoint) {
        userGameProgressRepository.save(
                new UserGameProgress(demoUser(), game, checkpoint)
        );
    }

    private JournalEntry saveEntry(User author, Checkpoint checkpoint) {
        return journalEntryRepository.save(
                new JournalEntry(
                        author,
                        checkpoint,
                        JournalEntryType.QUESTION,
                        "Una entrada para probar las respuestas."
                )
        );
    }
}
