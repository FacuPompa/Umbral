package com.umbral.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "user_game_progress",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"user_id", "game_id"}
                )
        }
)
public class UserGameProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checkpoint_id", nullable = false)
    private Checkpoint checkpoint;

    protected UserGameProgress() {
    }

    public UserGameProgress(User user,Game game, Checkpoint checkpoint) {
        this.user = user;
        this.game = game;
        this.checkpoint = checkpoint;
    }

    public void updateCheckpoint(Checkpoint checkpoint) {
        this.checkpoint = checkpoint;
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

    public Checkpoint getCheckpoint() {
        return checkpoint;
    }
}
