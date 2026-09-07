package com.umbral.domain.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(name = "rawg_game_id", unique = true)
    private Long rawgGameId;

    @Column(name = "cover_image_url", length = 1000)
    private String coverImageUrl;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    protected Game() {
    }

    public Game(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public Game(
            String title,
            String description,
            Long rawgGameId,
            String coverImageUrl,
            LocalDate releaseDate
    ) {
        this.title = title;
        this.description = description;
        this.rawgGameId = rawgGameId;
        this.coverImageUrl = coverImageUrl;
        this.releaseDate = releaseDate;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Long getRawgGameId() {
        return rawgGameId;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }
}
