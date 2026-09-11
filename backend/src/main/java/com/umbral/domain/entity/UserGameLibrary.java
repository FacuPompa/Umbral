package com.umbral.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_game_library")
public class UserGameLibrary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private UserGameLibraryStatus status;

    @Column(nullable = false)
    private boolean favorite;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserGameLibrary() {}

    public UserGameLibrary(User user, Game game, UserGameLibraryStatus status) {
        this.user = user;
        this.game = game;
        this.status = status;
        this.favorite = false;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Game getGame() {
        return game;
    }

    public UserGameLibraryStatus getStatus() {
        return status;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void updateFavorite(boolean favorite){
        this.favorite = favorite;
        this.updatedAt = Instant.now();
    }

    public void updateStatus(UserGameLibraryStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }
}
