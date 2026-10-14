package com.umbral.domain.service;

import com.umbral.domain.entity.Game;
import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserRole;
import com.umbral.domain.repository.GameRepository;
import com.umbral.domain.repository.UserRepository;
import com.umbral.support.PostgresTestConfiguration;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@Transactional
class CatalogAuditTest {
    @Autowired GameRepository gameRepository;
    @Autowired UserRepository userRepository;
    @Autowired EntityManager entityManager;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    @WithMockUser(username = "umbral-demo")
    void recordsCreationAndDifferentEditorWithoutChangingOriginalAuthor() {
        User creator = userRepository.findByHandle("umbral-demo").orElseThrow();
        Game game = gameRepository.saveAndFlush(new Game("Audited game", "Original description"));
        entityManager.refresh(game);
        var createdAt = game.getCreatedAt();
        assertEquals(creator.getId(), game.getCreatedByUserId());
        assertNotNull(createdAt);
        assertNull(game.getUpdatedAt());
        assertNull(game.getUpdatedByUserId());

        User editor = userRepository.saveAndFlush(new User("audit-editor", "audit@example.test",
                "unused", UserRole.MODERATOR));
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(editor.getHandle(), null,
                        AuthorityUtils.createAuthorityList("ROLE_MODERATOR")));
        ReflectionTestUtils.setField(game, "description", "Updated description");
        gameRepository.flush();
        entityManager.clear();
        Game reloaded = gameRepository.findById(game.getId()).orElseThrow();
        assertEquals(creator.getId(), reloaded.getCreatedByUserId());
        assertEquals(createdAt, reloaded.getCreatedAt());
        assertEquals(editor.getId(), reloaded.getUpdatedByUserId());
        assertNotNull(reloaded.getUpdatedAt());

        SecurityContextHolder.clearContext();
        ReflectionTestUtils.setField(reloaded, "description", "Technical update with no user");
        gameRepository.flush();
        entityManager.clear();
        Game technicalUpdate = gameRepository.findById(game.getId()).orElseThrow();
        assertNull(technicalUpdate.getUpdatedByUserId());
        assertEquals(creator.getId(), technicalUpdate.getCreatedByUserId());
        assertNotNull(technicalUpdate.getUpdatedAt());
    }

    @Test
    void legacySqlRecordHasUnknownMetadataAndRetainsItOnUpdate() {
        jdbcTemplate.update("INSERT INTO games (title, description) VALUES (?, ?)",
                "Legacy audit game", "Legacy description");
        Game game = gameRepository.findAll().stream()
                .filter(candidate -> candidate.getTitle().equals("Legacy audit game"))
                .findFirst().orElseThrow();
        assertNull(game.getCreatedAt());
        assertNull(game.getCreatedByUserId());
        assertNull(game.getUpdatedAt());
        ReflectionTestUtils.setField(game, "description", "Changed by a technical process");
        gameRepository.flush();
        entityManager.clear();
        Game reloaded = gameRepository.findById(game.getId()).orElseThrow();
        assertNull(reloaded.getCreatedAt());
        assertNull(reloaded.getCreatedByUserId());
        assertNull(reloaded.getUpdatedByUserId());
        assertNotNull(reloaded.getUpdatedAt());
    }

    @Test
    void anonymousTechnicalCreationDoesNotInventAnAuthor() {
        var previousContext = SecurityContextHolder.getContext();
        try {
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(new AnonymousAuthenticationToken("test", "anonymousUser",
                    AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
            SecurityContextHolder.setContext(context);
            Game game = gameRepository.saveAndFlush(new Game("Technical audit game", "Description"));
            assertNull(game.getCreatedByUserId());
            assertNotNull(game.getCreatedAt());
            assertNull(game.getUpdatedAt());
        } finally {
            SecurityContextHolder.setContext(previousContext);
        }
    }
}
