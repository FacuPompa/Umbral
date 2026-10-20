package com.umbral.domain.service;

import com.umbral.config.AccountPrincipal;
import com.umbral.domain.dto.*;
import com.umbral.domain.entity.*;
import com.umbral.domain.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;

@Service
public class RefreshTokenService {
    public static final long REFRESH_SECONDS = 7 * 24 * 60 * 60;
    private final SecureRandom random = new SecureRandom();
    private final RefreshGrantRepository grants;
    private final UsedRefreshTokenRepository used;
    private final UserRepository users;
    private final JwtTokenService jwt;
    private final EntityManager em;
    public record Issued(TokenResponse response, String refreshToken, Instant expiresAt) {
        @Override public String toString() { return "Issued[credentials=REDACTED]"; }
    }
    public RefreshTokenService(RefreshGrantRepository grants, UsedRefreshTokenRepository used, UserRepository users,
                               JwtTokenService jwt, EntityManager em) {
        this.grants = grants; this.used = used; this.users = users; this.jwt = jwt; this.em = em;
    }
    @Transactional
    public Issued login(AccountPrincipal principal) {
        var user = users.findForAccountModeration(principal.getUserId()).orElseThrow();
        em.refresh(user, LockModeType.PESSIMISTIC_WRITE);
        if (user.isSuspended() || user.getAuthenticationVersion() != principal.getAuthenticationVersion())
            throw new BadCredentialsException("Invalid account");
        String raw = randomToken();
        Instant now = Instant.now();
        var grant = grants.save(new RefreshGrant(user, hash(raw), now, now.plusSeconds(REFRESH_SECONDS)));
        return issued(grant, raw);
    }
    // Invalid/replayed tokens return empty AFTER committing revocation, not an exception that rolls it back.
    @Transactional
    public Optional<Issued> refresh(String raw) {
        var resolved = resolve(raw);
        if (resolved.isEmpty()) return Optional.empty();
        var grant = lock(resolved.get());
        if (grant == null) return Optional.empty();
        String hash = hash(raw);
        if (!valid(grant) || !grant.getTokenHash().equals(hash)) {
            grant.revoke();
            return Optional.empty();
        }
        used.save(new UsedRefreshToken(hash, grant));
        String replacement = randomToken();
        grant.rotate(hash(replacement));
        return Optional.of(issued(grant, replacement));
    }
    @Transactional
    public void logout(String raw, UUID authenticatedGrantId) {
        // One browser grant, not every device. Never revoke a grant solely from an untrusted ID.
        var resolved = resolve(raw);
        if (resolved.isPresent()) {
            var grant = lock(resolved.get());
            if (grant != null) grant.revoke();
        }
        if (authenticatedGrantId != null && (resolved.isEmpty() || !resolved.get().getId().equals(authenticatedGrantId))) {
            grants.findLockedById(authenticatedGrantId).ifPresent(RefreshGrant::revoke);
        }
    }
    private Optional<RefreshGrant> resolve(String raw) {
        if (raw == null || raw.length() != 43) return Optional.empty();
        String hash = hash(raw);
        return grants.findByTokenHash(hash).or(() -> used.findById(hash).map(UsedRefreshToken::getGrant));
    }
    private RefreshGrant lock(RefreshGrant snapshot) {
        // Consistent user -> grant lock order shared with password changes and moderation.
        var user = users.findForAccountModeration(snapshot.getUser().getId()).orElse(null);
        if (user == null) return null;
        em.refresh(user, LockModeType.PESSIMISTIC_WRITE);
        var grant = grants.findLockedById(snapshot.getId()).orElse(null);
        if (grant != null) em.refresh(grant, LockModeType.PESSIMISTIC_WRITE);
        return grant;
    }
    private boolean valid(RefreshGrant grant) {
        var user = grant.getUser();
        return !grant.isRevoked() && grant.getExpiresAt().isAfter(Instant.now())
                && !user.isSuspended() && grant.getAuthenticationVersion() == user.getAuthenticationVersion();
    }
    private Issued issued(RefreshGrant grant, String raw) {
        var user = grant.getUser();
        return new Issued(new TokenResponse(jwt.issue(grant), "Bearer", JwtTokenService.ACCESS_SECONDS,
                new AuthenticatedUserResponse(user.getId(), user.getHandle(), user.getEmail(), user.getRole())),
                raw, grant.getExpiresAt());
    }
    private String randomToken() {
        byte[] bytes = new byte[32]; random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    private static String hash(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
