package com.umbral.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "used_refresh_tokens")
public class UsedRefreshToken {
    @Id @Column(name = "token_hash", length = 64) private String tokenHash;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grant_id", nullable = false) private RefreshGrant grant;
    protected UsedRefreshToken() {}
    public UsedRefreshToken(String hash, RefreshGrant grant) { tokenHash = hash; this.grant = grant; }
    public RefreshGrant getGrant() { return grant; }
}
