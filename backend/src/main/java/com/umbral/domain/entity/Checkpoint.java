package com.umbral.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "checkpoints",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"game_id", "position"}
                ),
                @UniqueConstraint(
                        columnNames = {"id", "game_id"}
                )
        }
)
public class Checkpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false)
    private String label;

    @Column(
            nullable = false,
            check = @CheckConstraint(constraint = "position > 0")
    )
    private int position;

    protected Checkpoint() {
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

    public Checkpoint(Game game, String label, int position) {
        this.game = game;
        this.label = label;
        this.position = position;
    }
}
