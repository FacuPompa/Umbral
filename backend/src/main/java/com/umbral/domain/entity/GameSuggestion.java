package com.umbral.domain.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "game_suggestions",
        uniqueConstraints = @UniqueConstraint(columnNames = "rawg_game_id")
)
public class GameSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rawg_game_id", nullable = false)
    private Long rawgGameId;

    @Column(nullable = false)
    private String title;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "cover_image_url", length = 1000)
    private String coverImageUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "suggested_by_id", nullable = false)
    private User suggestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameSuggestionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    protected GameSuggestion() {
    }

    public GameSuggestion(
            Long rawgGameId,
            String title,
            LocalDate releaseDate,
            String coverImageUrl,
            User suggestedBy
    ) {
        this.rawgGameId = rawgGameId;
        this.title = title;
        this.releaseDate = releaseDate;
        this.coverImageUrl = coverImageUrl;
        this.suggestedBy = suggestedBy;
        this.status = GameSuggestionStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public void approve(User moderator) {
        status = GameSuggestionStatus.APPROVED;
        reviewedBy = moderator;
        reviewedAt = Instant.now();
    }

    public void reject(User moderator) {
        status = GameSuggestionStatus.REJECTED;
        reviewedBy = moderator;
        reviewedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getRawgGameId() {
        return rawgGameId;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public User getSuggestedBy() {
        return suggestedBy;
    }

    public GameSuggestionStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public User getReviewedBy() {
        return reviewedBy;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }
}
