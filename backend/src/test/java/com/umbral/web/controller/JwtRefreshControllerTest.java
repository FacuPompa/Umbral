package com.umbral.web.controller;

import com.jayway.jsonpath.JsonPath;
import com.umbral.config.JwtConfiguration;
import com.umbral.domain.repository.*;
import com.umbral.support.PostgresTestConfiguration;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class JwtRefreshControllerTest {
    @Autowired MockMvc mvc;
    @Autowired RefreshGrantRepository grants;
    @Autowired UsedRefreshTokenRepository used;
    @Autowired UserRepository users;
    @Autowired JwtEncoder encoder;
    @Autowired JwtDecoder decoder;
    private static final AtomicInteger SEQUENCE = new AtomicInteger();
    record Login(String access, Cookie refresh, String handle) {}

    @Test void loginIsStatelessAndExposesOnlyAnAccessTokenInTheBody() throws Exception {
        Login login = login();
        var jwt = decoder.decode(login.access());
        assertEquals("access", jwt.getClaimAsString("kind"));
        assertEquals(List.of("umbral-api"), jwt.getAudience());
        assertEquals(600, jwt.getExpiresAt().getEpochSecond() - jwt.getIssuedAt().getEpochSecond());
        assertNull(jwt.getClaim("password"));
        assertNull(jwt.getClaim("email"));
        assertTrue(login.refresh().isHttpOnly());
        assertEquals("/api/auth", login.refresh().getPath());
        assertEquals("Lax", login.refresh().getAttribute("SameSite"));
        assertNotEquals(login.refresh().getValue(), grants.findAll().getFirst().getTokenHash());
        mvc.perform(get("/api/auth/me").cookie(login.refresh())).andExpect(status().isUnauthorized());
        me(login.access()).andExpect(status().isOk());
    }

    @Test void refreshRotatesAndReplayingAnOldTokenCommitsFamilyRevocation() throws Exception {
        Login login = login();
        MvcResult rotated = refresh(login.refresh()).andExpect(status().isOk()).andReturn();
        Cookie replacement = rotated.getResponse().getCookie(AuthCookies.REFRESH);
        String access = JsonPath.read(rotated.getResponse().getContentAsString(), "$.accessToken");
        assertNotEquals(login.refresh().getValue(), replacement.getValue());
        assertEquals(1, used.count());
        refresh(login.refresh()).andExpect(status().isUnauthorized());
        assertTrue(grants.findAll().getFirst().isRevoked());
        refresh(replacement).andExpect(status().isUnauthorized());
        me(access).andExpect(status().isUnauthorized());
    }

    @Test void logoutRevokesOnlyThatDeviceIncludingItsAccessToken() throws Exception {
        Login first = login();
        Login second = loginExisting(first.handle());
        mvc.perform(post("/api/auth/logout").with(csrf()).cookie(first.refresh()))
                .andExpect(status().isNoContent());
        me(first.access()).andExpect(status().isUnauthorized());
        refresh(first.refresh()).andExpect(status().isUnauthorized());
        me(second.access()).andExpect(status().isOk());
        refresh(second.refresh()).andExpect(status().isOk());
    }

    @Test void refreshAndLogoutAlwaysRequireRealCsrfEvenWithBearerHeader() throws Exception {
        Login login = login();
        for (String path : List.of("refresh", "logout")) {
            mvc.perform(post("/api/auth/" + path).cookie(login.refresh())
                            .header("Authorization", "Bearer " + login.access())).andExpect(status().isForbidden());
            mvc.perform(post("/api/auth/" + path).cookie(login.refresh())
                            .cookie(new Cookie("XSRF-TOKEN", "not-valid")).header("X-XSRF-TOKEN", "invalid"))
                    .andExpect(status().isForbidden());
        }
        assertFalse(grants.findAll().getFirst().isRevoked());
    }

    @Test void rejectsExpiredTamperedWrongIssuerWrongAudienceAndMissingExpiryTokens() throws Exception {
        Login login = login();
        var valid = decoder.decode(login.access());
        var claims = valid.getClaims();
        List<JwtClaimsSet> invalid = new ArrayList<>();
        invalid.add(JwtClaimsSet.builder().claims(c -> c.putAll(claims)).issuedAt(Instant.now().minusSeconds(600))
                .expiresAt(Instant.now().minusSeconds(1)).build());
        invalid.add(JwtClaimsSet.builder().claims(c -> c.putAll(claims)).issuer("other").build());
        invalid.add(JwtClaimsSet.builder().claims(c -> c.putAll(claims)).audience(List.of("other")).build());
        invalid.add(JwtClaimsSet.builder().claims(c -> { c.putAll(claims); c.remove("exp"); }).build());
        invalid.add(JwtClaimsSet.builder().claims(c -> c.putAll(claims)).claim("kind", "refresh").build());
        invalid.add(JwtClaimsSet.builder().claims(c -> c.putAll(claims)).claim("ver", "invalid").build());
        invalid.add(JwtClaimsSet.builder().claims(c -> c.putAll(claims)).claim("ver", 999).build());
        invalid.add(JwtClaimsSet.builder().claims(c -> c.putAll(claims)).claim("sid", UUID.randomUUID().toString()).build());
        for (var bad : invalid) {
            String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), bad)).getTokenValue();
            me(token).andExpect(status().isUnauthorized());
        }
        String[] parts = login.access().split("\\.");
        me(parts[0] + "." + parts[1] + ".invalid-signature").andExpect(status().isUnauthorized());
        me("malformed").andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer")).andExpect(status().isUnauthorized());
    }

    @Test void passwordChangeInvalidatesEveryAccessAndRefreshToken() throws Exception {
        Login login = login();
        Login second = loginExisting(login.handle());
        mvc.perform(post("/api/me/password").header("Authorization", "Bearer " + login.access())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"test-password\",\"newPassword\":\"replacement-password\"}"))
                .andExpect(status().isNoContent());
        me(second.access()).andExpect(status().isUnauthorized());
        refresh(second.refresh()).andExpect(status().isUnauthorized());
        refresh(login.refresh()).andExpect(status().isUnauthorized());
    }

    @Test void unknownMissingAndExpiredRefreshTokensCannotAuthenticate() throws Exception {
        mvc.perform(post("/api/auth/refresh").with(csrf())).andExpect(status().isUnauthorized());
        refresh(new Cookie(AuthCookies.REFRESH, "x".repeat(43))).andExpect(status().isUnauthorized());
        Login login = login();
        // Actual expiration is enforced on both renewal and access authentication.
        var grant = grants.findAll().getFirst();
        // Access and refresh expiry tested via persisted SQL in a real transaction.
        expire(grant.getId());
        refresh(login.refresh()).andExpect(status().isUnauthorized());
        me(login.access()).andExpect(status().isUnauthorized());
    }

    @Autowired com.umbral.config.RefreshGrantMaintenance maintenance;
    @Test void purgesOnlyExpiredFamiliesAndTheirUsedTokenHashes() throws Exception {
        Login login = login();
        refresh(login.refresh()).andExpect(status().isOk());
        Login active = loginExisting(login.handle());
        UUID expiredId = UUID.fromString(decoder.decode(login.access()).getClaimAsString("sid"));
        expire(expiredId);
        maintenance.removeExpired();
        assertFalse(grants.existsById(expiredId));
        assertEquals(0, used.count());
        me(active.access()).andExpect(status().isOk());
    }

    @Test void suspensionAndReactivationCannotResurrectOldRefreshTokens() throws Exception {
        Login login = login();
        var moderator = users.save(new com.umbral.domain.entity.User("jwt-mod", "jwt-mod@example.test", "unused",
                com.umbral.domain.entity.UserRole.MODERATOR));
        var user = users.findByHandle(login.handle()).orElseThrow();
        user.suspend("Test suspension", moderator);
        users.saveAndFlush(user);
        me(login.access()).andExpect(status().isUnauthorized());
        refresh(login.refresh()).andExpect(status().isUnauthorized());
        user = users.findByHandle(login.handle()).orElseThrow();
        user.reactivate(moderator);
        users.saveAndFlush(user);
        refresh(login.refresh()).andExpect(status().isUnauthorized());
        me(loginExisting(login.handle()).access()).andExpect(status().isOk());
    }

    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;
    private void expire(UUID id) { jdbc.update("update refresh_grants set expires_at = now() - interval '1 second' where id = ?", id); }

    private Login login() throws Exception {
        String handle = "jwt-user-" + SEQUENCE.incrementAndGet();
        mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"handle\":\"" + handle + "\",\"email\":\"" + handle + "@example.test\",\"password\":\"test-password\"}"))
                .andExpect(status().isCreated());
        return loginExisting(handle);
    }
    private Login loginExisting(String handle) throws Exception {
        var result = mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"handle\":\"" + handle + "\",\"password\":\"test-password\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(header().string("Cache-Control", "no-store")).andReturn();
        assertNull(result.getRequest().getSession(false));
        return new Login(JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken"),
                result.getResponse().getCookie(AuthCookies.REFRESH), handle);
    }
    private ResultActions refresh(Cookie cookie) throws Exception {
        return mvc.perform(post("/api/auth/refresh").with(csrf()).cookie(cookie));
    }
    private ResultActions me(String access) throws Exception {
        return mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + access));
    }
    private RequestPostProcessor csrf() throws Exception {
        var result = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
        Cookie cookie = result.getResponse().getCookie("XSRF-TOKEN");
        String token = JsonPath.read(result.getResponse().getContentAsString(), "$.token");
        return request -> {
            var all = new ArrayList<Cookie>();
            if (request.getCookies() != null) all.addAll(List.of(request.getCookies()));
            all.add(cookie); request.setCookies(all.toArray(Cookie[]::new));
            request.addHeader("X-XSRF-TOKEN", token); return request;
        };
    }
}
