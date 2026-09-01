package com.umbral.domain.repository;

import com.umbral.domain.entity.Checkpoint;
import com.umbral.domain.entity.Game;
import com.umbral.domain.entity.JournalEntry;
import com.umbral.domain.entity.JournalEntryType;
import com.umbral.domain.entity.JournalReply;
import com.umbral.domain.entity.User;
import com.umbral.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@Transactional
class JournalReplyRepositoryTest {

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

    @Test
    void returnsRepliesOfAnEntryOrderedByCreation() {
        Game persona5Royal = gameRepository.findAll().stream()
                .filter(game -> game.getTitle().equals("Persona 5 Royal"))
                .findFirst()
                .orElseThrow();
        Checkpoint madarame = checkpointRepository
                .findByGameIdOrderByPositionAsc(persona5Royal.getId())
                .get(2);
        User demoUser = userRepository.findById(1L)
                .orElseThrow();

        JournalEntry entry = journalEntryRepository.save(
                new JournalEntry(
                        demoUser,
                        madarame,
                        JournalEntryType.QUESTION,
                        "Una entrada para probar respuestas."
                )
        );
        JournalReply firstReply = journalReplyRepository.save(
                new JournalReply(entry, demoUser, "Primera respuesta.")
        );
        JournalReply secondReply = journalReplyRepository.save(
                new JournalReply(entry, demoUser, "Segunda respuesta.")
        );

        List<JournalReply> replies = journalReplyRepository
                .findByJournalEntryIdOrderByCreatedAtAscIdAsc(entry.getId());

        assertEquals(2, replies.size());
        assertEquals(firstReply.getId(), replies.get(0).getId());
        assertEquals(secondReply.getId(), replies.get(1).getId());
    }
}
