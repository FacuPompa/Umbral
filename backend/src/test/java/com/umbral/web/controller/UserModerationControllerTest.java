package com.umbral.web.controller;

import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserRole;
import com.umbral.domain.repository.UserRepository;
import com.umbral.domain.repository.JournalEntryRepository;
import com.umbral.domain.repository.UserGameLibraryRepository;
import com.umbral.domain.repository.UserGameProgressRepository;
import com.umbral.domain.dto.SuspendUserRequest;
import com.umbral.domain.service.UserModerationService;
import com.umbral.support.PostgresTestConfiguration;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import com.jayway.jsonpath.JsonPath;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class UserModerationControllerTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired UserModerationService service;
    @Autowired PasswordEncoder encoder;
    @Autowired EntityManager em;
    @Autowired JournalEntryRepository entries;
    @Autowired UserGameLibraryRepository library;
    @Autowired UserGameProgressRepository progress;

    @Test
    void protectsQueriesAndActionsFromAnonymousAndMembers() throws Exception {
        User member = member("protected-member");
        mvc.perform(get("/api/moderation/users")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/moderation/users").with(user(member.getHandle())))
                .andExpect(status().isForbidden());
        for (String action : new String[]{"suspend", "reactivate"}) {
            mvc.perform(post("/api/moderation/users/{id}/{action}", member.getId(), action)
                            .with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"Reason\"}"))
                    .andExpect(status().isUnauthorized());
            mvc.perform(post("/api/moderation/users/{id}/{action}", member.getId(), action)
                            .with(user(member.getHandle())).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"Reason\"}"))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void suspendsAndReactivatesWithLastDecisionMetadataAndIdempotentRetries() throws Exception {
        User moderator = moderator();
        User member = member("status-member");
        suspend(moderator, member).andExpect(status().isOk())
                .andExpect(jsonPath("$.suspended").value(true))
                .andExpect(jsonPath("$.suspensionReason").value("Incumplimiento de las normas"))
                .andExpect(jsonPath("$.statusChangedByHandle").value(moderator.getHandle()))
                .andExpect(jsonPath("$.statusChangedAt").isNotEmpty())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.authenticationVersion").doesNotExist());
        em.flush();
        em.clear();
        User persisted = users.findById(member.getId()).orElseThrow();
        var suspendedAt = persisted.getAccountStatusChangedAt();
        assertEquals(1L, persisted.getAuthenticationVersion());
        suspend(moderator, persisted).andExpect(status().isOk());
        assertEquals(1L, persisted.getAuthenticationVersion());
        assertEquals(suspendedAt, persisted.getAccountStatusChangedAt());

        reactivate(moderator, persisted).andExpect(status().isOk())
                .andExpect(jsonPath("$.suspended").value(false))
                .andExpect(jsonPath("$.suspensionReason").isEmpty());
        assertEquals(2L, persisted.getAuthenticationVersion());
        reactivate(moderator, persisted).andExpect(status().isOk());
        assertEquals(2L, persisted.getAuthenticationVersion());
    }

    @Test
    void protectsSelfAndOtherModeratorsEvenForReactivation() throws Exception {
        User moderator = moderator();
        User other = users.save(new User("another-moderator", "another@example.test", "unused", UserRole.MODERATOR));
        for (User target : new User[]{moderator, other}) {
            suspend(moderator, target).andExpect(status().isConflict());
            reactivate(moderator, target).andExpect(status().isConflict());
            assertFalse(target.isSuspended());
            assertEquals(0L, target.getAuthenticationVersion());
        }
    }

    @Test
    void rejectsMissingWhitespaceShortAndOversizedReasons() throws Exception {
        User moderator = moderator();
        User member = member("reason-member");
        for (String body : new String[]{"{}", "{\"reason\":\"\"}", "{\"reason\":\"    \"}",
                "{\"reason\":\" a \"}", "{\"reason\":\"ab\"}", "{\"reason\":\"" + "a".repeat(501) + "\"}"}) {
            mvc.perform(post("/api/moderation/users/{id}/suspend", member.getId())
                            .with(user(moderator.getHandle()).roles("MODERATOR")).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        assertFalse(member.isSuspended());
    }

    @Test
    void requiresCsrfForBothActions() throws Exception {
        User moderator = moderator();
        User member = member("csrf-member");
        for (String action : new String[]{"suspend", "reactivate"}) {
            mvc.perform(post("/api/moderation/users/{id}/{action}", member.getId(), action)
                            .with(user(moderator.getHandle()).roles("MODERATOR"))
                            .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"Valid reason\"}"))
                    .andExpect(status().isForbidden());
        }
        assertFalse(member.isSuspended());
    }

    @Test
    void queriesCaseInsensitivelyFiltersAndPaginatesWithoutPrivateCredentials() throws Exception {
        User moderator = moderator();
        User first = member("zzfilter-A");
        member("zzfilter-B");
        suspend(moderator, first).andExpect(status().isOk());
        mvc.perform(get("/api/moderation/users?query=ZZFILTER&size=1")
                        .with(user(moderator.getHandle()).roles("MODERATOR")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].handle").value(first.getHandle()))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.items[0].email").doesNotExist())
                .andExpect(jsonPath("$.items[0].passwordHash").doesNotExist());
        mvc.perform(get("/api/moderation/users?query=zzfilter&suspended=true")
                        .with(user(moderator.getHandle()).roles("MODERATOR")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].statusChangedByHandle").value(moderator.getHandle()));
        mvc.perform(get("/api/moderation/users?query=zzfilter&suspended=false")
                        .with(user(moderator.getHandle()).roles("MODERATOR")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].handle").value("zzfilter-B"));
        mvc.perform(get("/api/moderation/users?query=zzfilter&size=1&page=1")
                        .with(user(moderator.getHandle()).roles("MODERATOR")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void validatesQueryBoundsAndMissingTargets() throws Exception {
        User moderator = moderator();
        for (String query : new String[]{"page=-1", "page=10001", "size=0", "size=51", "suspended=other",
                "query=" + "a".repeat(31)}) {
            mvc.perform(get("/api/moderation/users?" + query)
                            .with(user(moderator.getHandle()).roles("MODERATOR")))
                    .andExpect(status().isBadRequest());
        }
        for (String action : new String[]{"suspend", "reactivate"}) {
            mvc.perform(post("/api/moderation/users/999999/{action}", action)
                            .with(user(moderator.getHandle()).roles("MODERATOR")).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"Valid reason\"}"))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void checksTheDatabaseRoleAndNotOnlySessionAuthorities() throws Exception {
        User member = member("forged-role");
        mvc.perform(get("/api/moderation/users").with(user(member.getHandle()).roles("MODERATOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "umbral-demo")
    void alsoProtectsDirectServiceCalls() {
        assertThrows(AccessDeniedException.class, () -> service.search("", null, 0, 10));
        assertThrows(AccessDeniedException.class, () -> service.suspend(1L, new SuspendUserRequest("Valid reason")));
        assertThrows(AccessDeniedException.class, () -> service.reactivate(1L));
    }

    @Test
    void rejectsLegacySessionIdentitiesThatNoLongerExist() throws Exception {
        mvc.perform(get("/api/auth/me").with(user("missing-account")))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/moderation/users").with(user("missing-account").roles("MODERATOR")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void blocksLoginAndEveryPreSuspensionSessionEvenAfterReactivation() throws Exception {
        User moderator = moderator();
        User member = member("real-session");
        String first = login(member.getHandle());

        String untouched = login(member.getHandle());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + first)).andExpect(status().isOk());
        suspend(moderator, member).andExpect(status().isOk());
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"handle\":\"real-session\",\"password\":\"test-password\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/me/library").header("Authorization", "Bearer " + first).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"gameId\":1,\"status\":\"PLAYING\"}"))
                .andExpect(status().isUnauthorized());
        reactivate(moderator, member).andExpect(status().isOk());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + untouched)).andExpect(status().isUnauthorized());
        String fresh = login(member.getHandle());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + fresh)).andExpect(status().isOk());
    }

    @Test
    void leavesPublicDataAndPersonalContentUntouched() throws Exception {
        User moderator = moderator();
        User member = users.findByHandle("umbral-demo").orElseThrow();
        long entryCount = entries.count();
        long libraryCount = library.count();
        long progressCount = progress.count();
        suspend(moderator, member).andExpect(status().isOk());
        assertEquals(entryCount, entries.count());
        assertEquals(libraryCount, library.count());
        assertEquals(progressCount, progress.count());
        mvc.perform(get("/api/users/{handle}", member.getHandle())).andExpect(status().isOk())
                .andExpect(jsonPath("$.suspended").doesNotExist())
                .andExpect(jsonPath("$.suspensionReason").doesNotExist());
        mvc.perform(get("/api/games")).andExpect(status().isOk());
    }

    private User moderator() {
        return users.save(new User("account-moderator", "moderator@example.test", encoder.encode("test-password"), UserRole.MODERATOR));
    }

    private User member(String handle) {
        return users.save(new User(handle, handle + "@example.test", encoder.encode("test-password"), UserRole.MEMBER));
    }

    private org.springframework.test.web.servlet.ResultActions suspend(User moderator, User target) throws Exception {
        return mvc.perform(post("/api/moderation/users/{id}/suspend", target.getId())
                .with(user(moderator.getHandle()).roles("MODERATOR")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"  Incumplimiento de las normas  \"}"));
    }

    private org.springframework.test.web.servlet.ResultActions reactivate(User moderator, User target) throws Exception {
        return mvc.perform(post("/api/moderation/users/{id}/reactivate", target.getId())
                .with(user(moderator.getHandle()).roles("MODERATOR")).with(csrf()));
    }

    private String login(String handle) throws Exception {
        return JsonPath.read(mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"handle\":\"" + handle + "\",\"password\":\"test-password\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), "$.accessToken");
    }
}
