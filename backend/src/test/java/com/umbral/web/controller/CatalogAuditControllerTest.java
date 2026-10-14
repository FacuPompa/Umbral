package com.umbral.web.controller;

import com.umbral.domain.entity.Game;
import com.umbral.domain.entity.Checkpoint;
import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserRole;
import com.umbral.domain.repository.GameRepository;
import com.umbral.domain.repository.CheckpointRepository;
import com.umbral.domain.repository.UserRepository;
import com.umbral.domain.service.CatalogAuditService;
import com.umbral.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@Transactional
class CatalogAuditControllerTest {
    @Autowired org.springframework.test.web.servlet.MockMvc mockMvc;
    @Autowired GameRepository gameRepository;
    @Autowired CheckpointRepository checkpointRepository;
    @Autowired UserRepository userRepository;
    @Autowired CatalogAuditService catalogAuditService;

    @Test
    void anonymousAndMembersCannotReadEitherAuditEndpoint() throws Exception {
        for (String path : new String[]{"/api/moderation/games/1/audit",
                "/api/moderation/checkpoints/1/audit"}) {
            mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
            mockMvc.perform(get(path).with(user("umbral-demo")))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void moderatorCanReadLegacyMetadataWithoutExposingEmailOrPassword() throws Exception {
        User moderator = moderator();
        Game game = gameRepository.findAll().getFirst();
        Long checkpointId = checkpointRepository.findByGameIdOrderByPositionAsc(game.getId())
                .getFirst().getId();
        for (String path : new String[]{"/api/moderation/games/" + game.getId() + "/audit",
                "/api/moderation/checkpoints/" + checkpointId + "/audit"}) {
            mockMvc.perform(get(path).with(user(moderator.getHandle()).roles("MODERATOR")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.createdByUserId").isEmpty())
                    .andExpect(jsonPath("$.createdAt").isEmpty())
                    .andExpect(jsonPath("$.updatedByUserId").isEmpty())
                    .andExpect(jsonPath("$.updatedAt").isEmpty())
                    .andExpect(jsonPath("$.email").doesNotExist())
                    .andExpect(jsonPath("$.passwordHash").doesNotExist());
        }
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(
            username = "audit-moderator", roles = "MODERATOR")
    void exposesTheActualCreatorAndCreationDateToModerators() throws Exception {
        User moderator = moderator();
        Game game = gameRepository.saveAndFlush(new Game("Audit response game", "Safe description"));
        Checkpoint checkpoint = checkpointRepository.saveAndFlush(new Checkpoint(game, "Safe checkpoint", 1));
        for (String path : new String[]{"/api/moderation/games/" + game.getId() + "/audit",
                "/api/moderation/checkpoints/" + checkpoint.getId() + "/audit"}) {
            mockMvc.perform(get(path).with(user(moderator.getHandle()).roles("MODERATOR")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.createdByUserId").value(moderator.getId()))
                    .andExpect(jsonPath("$.createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.updatedByUserId").isEmpty())
                    .andExpect(jsonPath("$.updatedAt").isEmpty());
        }
    }

    @Test
    void staleModeratorAuthorityCannotOverrideTheRoleInTheDatabase() throws Exception {
        mockMvc.perform(get("/api/moderation/games/1/audit")
                        .with(user("umbral-demo").roles("MODERATOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void missingResourcesReturnNotFoundForModerators() throws Exception {
        User moderator = moderator();
        for (String path : new String[]{"/api/moderation/games/999999/audit",
                "/api/moderation/checkpoints/999999/audit"}) {
            mockMvc.perform(get(path).with(user(moderator.getHandle()).roles("MODERATOR")))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "umbral-demo")
    void methodSecurityAlsoProtectsDirectServiceCalls() {
        assertThrows(AccessDeniedException.class, () -> catalogAuditService.getGameAudit(1L));
        assertThrows(AccessDeniedException.class, () -> catalogAuditService.getCheckpointAudit(1L));
    }

    @Test
    void publicCatalogDoesNotExposeAuditMetadata() throws Exception {
        Game game = gameRepository.findAll().getFirst();
        mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].createdByUserId").doesNotExist())
                .andExpect(jsonPath("$[0].createdAt").doesNotExist());
        mockMvc.perform(get("/api/games/{gameId}/checkpoints", game.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].createdByUserId").doesNotExist())
                .andExpect(jsonPath("$[0].createdAt").doesNotExist());
    }

    private User moderator() {
        return userRepository.save(new User("audit-moderator", "moderator@example.test",
                "unused", UserRole.MODERATOR));
    }
}
