package com.umbral.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "app_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String handle;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private boolean suspended;

    @Column(name = "authentication_version", nullable = false)
    private long authenticationVersion;

    @Version
    @Column(name = "row_version", nullable = false)
    private long rowVersion;

    @Column(name = "suspension_reason", length = 500)
    private String suspensionReason;

    @Column(name = "account_status_changed_at")
    private Instant accountStatusChangedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_status_changed_by_user_id")
    private User accountStatusChangedBy;

    protected User() {
    }

    public User(String handle, String email, String passwordHash, UserRole role) {
        this.handle = handle;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getHandle() {
        return handle;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isSuspended() { return suspended; }
    public long getAuthenticationVersion() { return authenticationVersion; }
    public String getSuspensionReason() { return suspensionReason; }
    public Instant getAccountStatusChangedAt() { return accountStatusChangedAt; }
    public User getAccountStatusChangedBy() { return accountStatusChangedBy; }

    public void suspend(String reason, User moderator) {
        if (suspended) return;
        String trimmedReason = reason == null ? "" : reason.trim();
        if (trimmedReason.length() < 3 || trimmedReason.length() > 500) {
            throw new IllegalArgumentException("El motivo debe tener entre 3 y 500 caracteres.");
        }
        suspended = true;
        suspensionReason = trimmedReason;
        changeAccountStatus(moderator);
    }

    public void reactivate(User moderator) {
        if (!suspended) return;
        suspended = false;
        suspensionReason = null;
        changeAccountStatus(moderator);
    }

    private void changeAccountStatus(User moderator) {
        authenticationVersion++;
        accountStatusChangedAt = Instant.now();
        accountStatusChangedBy = moderator;
    }

    public void changePassword(String passwordHash) {
        this.passwordHash = passwordHash;
        authenticationVersion++;
    }
}
