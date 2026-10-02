package com.umbral.domain.repository;

import com.umbral.domain.entity.Checkpoint;
import com.umbral.domain.entity.Game;
import com.umbral.domain.entity.JournalEntry;
import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserGameProgress;
import com.umbral.domain.entity.JournalEntryType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.umbral.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.Instant;

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
                .findVisibleByReaderIdAndGameId(1L, persona5Royal.getId(), null, newestPage(0, 10)).getContent();

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
                .findVisibleByReaderIdAndGameId(1L, persona5Royal.getId(), null, newestPage(0, 10)).getContent();

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
                .findVisibleByReaderIdAndGameId(1L, persona5Royal.getId(), null, newestPage(0, 10)).getContent();

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

    private PageRequest newestPage(int page, int size) {
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
    }

    @Test
    void filtersBeforePaginationAndDoesNotCountHiddenEntriesAsMoreResults() {
        var fixture = savePaginationFixture();
        var first = journalEntryRepository.findVisibleByReaderIdAndGameId(
                1L, fixture.game().getId(), JournalEntryType.QUESTION, newestPage(0, 1));
        var second = journalEntryRepository.findVisibleByReaderIdAndGameId(
                1L, fixture.game().getId(), JournalEntryType.QUESTION, newestPage(1, 1));
        var beyondEnd = journalEntryRepository.findVisibleByReaderIdAndGameId(
                1L, fixture.game().getId(), JournalEntryType.QUESTION, newestPage(2, 1));

        assertEquals(List.of(fixture.secondQuestion().getId()), first.map(JournalEntry::getId).getContent());
        assertTrue(first.hasNext());
        assertEquals(List.of(fixture.firstQuestion().getId()), second.map(JournalEntry::getId).getContent());
        assertTrue(!second.hasNext());
        assertTrue(beyondEnd.isEmpty());
        assertTrue(!beyondEnd.hasNext());
    }

    @Test
    void sortsEqualTimestampsByIdInBothDirectionsWithoutRepeatingEntries() {
        var fixture = savePaginationFixture();
        var oldest = journalEntryRepository.findVisibleByReaderIdAndGameId(
                1L, fixture.game().getId(), JournalEntryType.QUESTION,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdAt", "id")));
        var newest = journalEntryRepository.findVisibleByReaderIdAndGameId(
                1L, fixture.game().getId(), JournalEntryType.QUESTION, newestPage(0, 10));
        assertEquals(List.of(fixture.firstQuestion().getId(), fixture.secondQuestion().getId()),
                oldest.map(JournalEntry::getId).getContent());
        assertEquals(List.of(fixture.secondQuestion().getId(), fixture.firstQuestion().getId()),
                newest.map(JournalEntry::getId).getContent());
    }

    @Test
    void unfilteredPagesStillExcludeOtherGamesAndFutureCheckpoints() {
        var fixture = savePaginationFixture();
        var entries = journalEntryRepository.findVisibleByReaderIdAndGameId(
                1L, fixture.game().getId(), null, newestPage(0, 10));
        assertEquals(3, entries.getNumberOfElements());
        assertTrue(entries.stream().allMatch(entry -> entry.getCheckpoint().getPosition() == 1));
        assertTrue(entries.stream().allMatch(entry -> entry.getCheckpoint().getGame().getId().equals(fixture.game().getId())));
        assertTrue(!entries.hasNext());
    }

    private PaginationFixture savePaginationFixture() {
        User author = userRepository.findById(1L).orElseThrow();
        Game game = gameRepository.save(new Game("Pagination test", "Test description"));
        Checkpoint visible = checkpointRepository.save(new Checkpoint(game, "Visible", 1));
        Checkpoint hidden = checkpointRepository.save(new Checkpoint(game, "Hidden", 2));
        saveProgressForDemoUser(game, visible);
        JournalEntry first = saveEntryAt(author, visible, JournalEntryType.QUESTION, "First", "2026-09-01T00:00:00Z");
        JournalEntry second = saveEntryAt(author, visible, JournalEntryType.QUESTION, "Second", "2026-09-01T00:00:00Z");
        saveEntryAt(author, visible, JournalEntryType.REVIEW, "Review", "2026-09-02T00:00:00Z");
        saveEntryAt(author, hidden, JournalEntryType.QUESTION, "Hidden question", "2026-09-03T00:00:00Z");
        Game otherGame = gameRepository.save(new Game("Other pagination game", "Other description"));
        Checkpoint otherCheckpoint = checkpointRepository.save(new Checkpoint(otherGame, "Other", 1));
        saveEntryAt(author, otherCheckpoint, JournalEntryType.QUESTION, "Other game", "2026-09-04T00:00:00Z");
        journalEntryRepository.flush();
        return new PaginationFixture(game, first, second);
    }

    private JournalEntry saveEntryAt(User author, Checkpoint checkpoint, JournalEntryType type, String content, String date) {
        JournalEntry entry = new JournalEntry(author, checkpoint, type, content);
        ReflectionTestUtils.setField(entry, "createdAt", Instant.parse(date));
        return journalEntryRepository.save(entry);
    }

    private record PaginationFixture(Game game, JournalEntry firstQuestion, JournalEntry secondQuestion) {}

    private void saveProgressForDemoUser(Game game, Checkpoint checkpoint) {
        User demoUser = userRepository.findById(1L)
                .orElseThrow();

        userGameProgressRepository.save(
                new UserGameProgress(demoUser, game, checkpoint)
        );
    }
}
