package com.umbral.domain.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "checkpoint_suggestions")
public class CheckpointSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private int position;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "suggested_by_id", nullable = false)
    private User suggestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckpointSuggestionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    protected CheckpointSuggestion() {
    }

    public CheckpointSuggestion(Game game, String label, int position, User suggestedBy) {
        this.game = game;
        this.label = label;
        this.position = position;
        this.suggestedBy = suggestedBy;
        this.status = CheckpointSuggestionStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public void approve(User moderator) {
        status = CheckpointSuggestionStatus.APPROVED;
        reviewedBy = moderator;
        reviewedAt = Instant.now();
    }

    public void reject(User moderator) {
        status = CheckpointSuggestionStatus.REJECTED;
        reviewedBy = moderator;
        reviewedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Game getGame() {
        return game;
    }

    public String getLabel() {
        return label;
    }

    public int getPosition() {
        return position;
    }

    public User getSuggestedBy() {
        return suggestedBy;
    }

    public CheckpointSuggestionStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
