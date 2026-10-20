package com.umbral.web.controller;

import com.umbral.domain.dto.ChangePasswordRequest;
import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserRole;
import com.umbral.domain.repository.UserRepository;
import com.umbral.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import com.jayway.jsonpath.JsonPath;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class PasswordChangeControllerTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;

    @Test
    void changesOnlyOwnHashRevokesAllOldSessionsAndPreservesAccountData() throws Exception {
        User owner = account("password-owner", UserRole.MEMBER);
        User other = account("password-other", UserRole.MEMBER);
        String otherHash = other.getPasswordHash();
        String current = login(owner.getHandle(), "old-password");
        String second = login(owner.getHandle(), "old-password");
        mvc.perform(post("/api/me/password").header("Authorization", "Bearer " + current).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"old-password\",\"newPassword\":\" new-password \",\"userId\":" + other.getId() + "}"))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + current)).andExpect(status().isUnauthorized());
        users.flush();
        assertTrue(encoder.matches(" new-password ", owner.getPasswordHash()));
        assertFalse(encoder.matches("new-password", owner.getPasswordHash()));
        assertEquals(1L, owner.getAuthenticationVersion());
        assertEquals(UserRole.MEMBER, owner.getRole());
        assertFalse(owner.isSuspended());
        assertNull(owner.getAccountStatusChangedAt());
        assertEquals(otherHash, other.getPasswordHash());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + second)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"handle\":\"password-owner\",\"password\":\"old-password\"}"))
                .andExpect(status().isUnauthorized());
        String fresh = login(owner.getHandle(), " new-password ");
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + fresh)).andExpect(status().isOk());
    }

    @Test
    void incorrectCurrentPasswordReturnsUsefulErrorWithoutClosingValidSession() throws Exception {
        User owner = account("wrong-current", UserRole.MEMBER);
        String session = login(owner.getHandle(), "old-password");
        String oldHash = owner.getPasswordHash();
        change(session, "wrong-password", "new-password")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La contraseña actual no es correcta."));
        assertEquals(oldHash, owner.getPasswordHash());
        assertEquals(0L, owner.getAuthenticationVersion());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + session)).andExpect(status().isOk());
    }

    @Test
    void rejectsSameShortBlankMissingAndMultibyteOversizedNewPasswords() throws Exception {
        User owner = account("invalid-password", UserRole.MEMBER);
        String session = login(owner.getHandle(), "old-password");
        String original = owner.getPasswordHash();
        for (String next : new String[]{"old-password", "short", "        ", "é".repeat(37), "a".repeat(73)}) {
            change(session, "old-password", next).andExpect(status().isBadRequest());
            assertEquals(original, owner.getPasswordHash());
            assertEquals(0L, owner.getAuthenticationVersion());
        }
        mvc.perform(post("/api/me/password").header("Authorization", "Bearer " + session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + session)).andExpect(status().isOk());
    }

    @Test
    void requiresAuthenticationAndAcceptsBearerWithoutCookieCsrf() throws Exception {
        mvc.perform(post("/api/me/password").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"old-password\",\"newPassword\":\"new-password\"}"))
                .andExpect(status().isUnauthorized());
        User owner = account("csrf-password", UserRole.MEMBER);
        String session = login(owner.getHandle(), "old-password");
        mvc.perform(post("/api/me/password").header("Authorization", "Bearer " + session).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"old-password\",\"newPassword\":\"new-password\"}"))
                .andExpect(status().isNoContent());
        assertEquals(1L, owner.getAuthenticationVersion());
    }

    @Test
    void moderatorsCanChangeOwnPasswordAndSuspendedAccountsCannot() throws Exception {
        User moderator = account("password-moderator", UserRole.MODERATOR);
        change(login(moderator.getHandle(), "old-password"), "old-password", "new-password")
                .andExpect(status().isNoContent());
        assertEquals(UserRole.MODERATOR, moderator.getRole());
        User owner = account("suspended-password", UserRole.MEMBER);
        String session = login(owner.getHandle(), "old-password");
        owner.suspend("Account disabled for test", moderator);
        users.flush();
        String hash = owner.getPasswordHash();
        change(session, "old-password", "new-password").andExpect(status().isUnauthorized());
        assertEquals(hash, owner.getPasswordHash());
        assertEquals(1L, owner.getAuthenticationVersion());
    }

    @Test
    void redactsRequestCredentialsFromStringRepresentations() {
        String text = new ChangePasswordRequest("secret-current", "secret-new").toString();
        assertFalse(text.contains("secret-current"));
        assertFalse(text.contains("secret-new"));
    }

    private User account(String handle, UserRole role) {
        return users.save(new User(handle, handle + "@example.test", encoder.encode("old-password"), role));
    }

    private String login(String handle, String password) throws Exception {
        return JsonPath.read(mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"handle\":\"" + handle + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), "$.accessToken");
    }

    private org.springframework.test.web.servlet.ResultActions change(String session, String current, String next) throws Exception {
        return mvc.perform(post("/api/me/password").header("Authorization", "Bearer " + session).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"" + current + "\",\"newPassword\":\"" + next + "\"}"));
    }
}
