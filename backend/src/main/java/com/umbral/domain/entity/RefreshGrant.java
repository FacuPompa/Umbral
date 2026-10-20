package com.umbral.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_grants")
public class RefreshGrant {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(name = "token_hash", nullable = false, length = 64) private String tokenHash;
    @Column(name = "authentication_version", nullable = false) private long authenticationVersion;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(nullable = false) private boolean revoked;
    protected RefreshGrant() {}
    public RefreshGrant(User user, String hash, Instant now, Instant expiry) {
        id = UUID.randomUUID(); this.user = user; tokenHash = hash;
        authenticationVersion = user.getAuthenticationVersion(); createdAt = now; expiresAt = expiry;
    }
    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getTokenHash() { return tokenHash; }
    public long getAuthenticationVersion() { return authenticationVersion; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return revoked; }
    public void rotate(String hash) { tokenHash = hash; }
    public void revoke() { revoked = true; }
}
