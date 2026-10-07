package com.umbral.web.controller;

import com.umbral.domain.entity.*;
import com.umbral.domain.repository.*;
import com.umbral.support.PostgresTestConfiguration;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@Transactional
class JournalEntryEditingTest {
    @Autowired MockMvc mvc;
    @Autowired JournalEntryRepository entries;
    @Autowired JournalReplyRepository replies;
    @Autowired UserRepository users;
    @Autowired GameRepository games;
    @Autowired CheckpointRepository checkpoints;
    @Autowired UserGameProgressRepository progresses;
    @Autowired EntityManager entityManager;

    private record Fixture(User author, Game game, Checkpoint first, Checkpoint next, JournalEntry entry) {}

    private Fixture fixture() {
        User author = users.findByHandle("umbral-demo").orElseThrow();
        Game game = games.save(new Game("Juego de edición", "Prueba de edición."));
        Checkpoint first = checkpoints.save(new Checkpoint(game, "Primer tramo", 1));
        Checkpoint next = checkpoints.save(new Checkpoint(game, "Segundo tramo", 2));
        progresses.save(new UserGameProgress(author, game, first));
        JournalEntry entry = entries.save(new JournalEntry(author, first, JournalEntryType.REFLECTION, "Texto original"));
        return new Fixture(author, game, first, next, entry);
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder edit(Long id, String body) {
        return patch("/api/me/journal-entries/{id}", id).with(user("umbral-demo")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(body);
    }

    @Test
    void persistsContentTypeAndEditDateWithoutChangingCheckpointAuthorCreationDateOrReplies() throws Exception {
        Fixture f = fixture();
        entityManager.flush();
        entityManager.refresh(f.entry());
        var createdAt = f.entry().getCreatedAt();
        var reply = replies.save(new JournalReply(f.entry(), f.author(), "Respuesta existente"));
        mvc.perform(edit(f.entry().getId(), """
                {"type":"QUESTION", "content":"Texto corregido"}
                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.type").value("QUESTION"))
                .andExpect(jsonPath("$.content").value("Texto corregido"))
                .andExpect(jsonPath("$.editedAt").isNotEmpty())
                .andExpect(jsonPath("$.checkpointLabel").value("Primer tramo"));
        entityManager.flush();
        entityManager.clear();
        JournalEntry saved = entries.findById(f.entry().getId()).orElseThrow();
        assertEquals("Texto corregido", saved.getContent());
        assertEquals(JournalEntryType.QUESTION, saved.getType());
        assertEquals(f.first().getId(), saved.getCheckpoint().getId());
        assertEquals(f.author().getId(), saved.getAuthor().getId());
        assertEquals(createdAt, saved.getCreatedAt());
        assertNotNull(saved.getEditedAt());
        assertTrue(saved.getEditedAt().isAfter(saved.getCreatedAt()));
        assertEquals(saved.getId(), replies.findById(reply.getId()).orElseThrow().getJournalEntry().getId());
        mvc.perform(get("/api/games/{id}/journal-entries", f.game().getId()).with(user("umbral-demo")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].content").value("Texto corregido"))
                .andExpect(jsonPath("$.items[0].editedAt").isNotEmpty());
    }

    @Test
    void keepsUnmodifiedEntriesUnmarkedAndPreservesTheDateOnRepeatedIdenticalUpdates() throws Exception {
        Fixture f = fixture();
        mvc.perform(edit(f.entry().getId(), """
                {"type":"REFLECTION", "content":"Texto original"}
                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.editedAt").doesNotExist());
        assertNull(f.entry().getEditedAt());
        mvc.perform(edit(f.entry().getId(), """
                {"type":"REVIEW", "content":"Texto original"}
                """))
                .andExpect(status().isOk());
        var editedAt = f.entry().getEditedAt();
        assertNotNull(editedAt);
        mvc.perform(edit(f.entry().getId(), """
                {"type":"REVIEW", "content":"Texto original"}
                """))
                .andExpect(status().isOk());
        assertEquals(editedAt, f.entry().getEditedAt());
    }

    @Test
    void returns404ForForeignAndMissingEntriesWithoutModifyingThem() throws Exception {
        Fixture f = fixture();
        User other = users.findByHandle("umbral-author-demo").orElseThrow();
        JournalEntry foreign = entries.save(new JournalEntry(other, f.first(), JournalEntryType.THEORY, "Contenido ajeno"));
        for (Long id : new Long[]{foreign.getId(), Long.MAX_VALUE}) {
            mvc.perform(edit(id, """
                    {"type":"QUESTION", "content":"Intento de edición"}
                    """))
                    .andExpect(status().isNotFound());
        }
        assertEquals("Contenido ajeno", foreign.getContent());
        assertNull(foreign.getEditedAt());
    }

    @Test
    void moderatorsCannotEditOtherAuthorsEntries() throws Exception {
        Fixture f = fixture();
        users.save(new User("editing-mod", "editing-mod@example.test", "unused", UserRole.MODERATOR));
        mvc.perform(patch("/api/me/journal-entries/{id}", f.entry().getId())
                        .with(user("editing-mod").roles("MODERATOR")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"type":"QUESTION", "content":"Cambio ajeno"}
                                """))
                .andExpect(status().isNotFound());
        assertEquals("Texto original", f.entry().getContent());
    }

    @Test
    void cannotEditOrRevealAnOwnEntryBeyondCurrentProgress() throws Exception {
        Fixture f = fixture();
        JournalEntry future = entries.save(new JournalEntry(f.author(), f.next(), JournalEntryType.REVIEW, "Contenido futuro"));
        mvc.perform(edit(future.getId(), """
                {"type":"QUESTION", "content":"No debe guardarse"}
                """))
                .andExpect(status().isNotFound());
        assertEquals("Contenido futuro", future.getContent());
        assertNull(future.getEditedAt());
    }

    @Test
    void rejectsEditingWhenProgressWasRemoved() throws Exception {
        Fixture f = fixture();
        progresses.delete(progresses.findByUserIdAndGameId(f.author().getId(), f.game().getId()).orElseThrow());
        mvc.perform(edit(f.entry().getId(), """
                {"type":"QUESTION", "content":"No debe guardarse"}
                """))
                .andExpect(status().isNotFound());
        assertEquals("Texto original", f.entry().getContent());
    }

    @Test
    void ignoresAttemptsToMoveTheCheckpointOrChangeTheAuthor() throws Exception {
        Fixture f = fixture();
        mvc.perform(edit(f.entry().getId(), """
                {"type":"QUESTION", "content":"Corrección", "checkpointId":%d, "authorId":999}
                """.formatted(f.next().getId())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.checkpointLabel").value("Primer tramo"));
        assertEquals(f.first().getId(), f.entry().getCheckpoint().getId());
        assertEquals(f.author().getId(), f.entry().getAuthor().getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"type\":\"QUESTION\",\"content\":\" \"}",
            "{\"type\":\"QUESTION\"}",
            "{\"content\":\"Texto\"}",
            "{\"type\":\"INVALID\",\"content\":\"Texto\"}"
    })
    void rejectsInvalidContentAndType(String body) throws Exception {
        Fixture f = fixture();
        mvc.perform(edit(f.entry().getId(), body)).andExpect(status().isBadRequest());
        assertEquals("Texto original", f.entry().getContent());
        assertNull(f.entry().getEditedAt());
    }

    @Test
    void enforcesTheContentLengthLimit() throws Exception {
        Fixture f = fixture();
        mvc.perform(edit(f.entry().getId(), "{\"type\":\"QUESTION\",\"content\":\"" + "x".repeat(5001) + "\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(edit(f.entry().getId(), "{\"type\":\"QUESTION\",\"content\":\"" + "x".repeat(5000) + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void requiresAuthenticationAndCsrfForEditing() throws Exception {
        Fixture f = fixture();
        String body = "{\"type\":\"QUESTION\",\"content\":\"Corrección\"}";
        mvc.perform(patch("/api/me/journal-entries/{id}", f.entry().getId()).with(anonymous()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mvc.perform(patch("/api/me/journal-entries/{id}", f.entry().getId()).with(user("umbral-demo"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/me/journal-entries/{id}", f.entry().getId()).with(user("umbral-demo")).with(csrf().useInvalidToken())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        assertEquals("Texto original", f.entry().getContent());
    }
}
