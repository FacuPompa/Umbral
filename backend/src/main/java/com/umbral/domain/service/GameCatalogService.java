package com.umbral.domain.service;

import com.umbral.domain.dto.CheckpointResponse;
import com.umbral.domain.dto.GameResponse;
import com.umbral.domain.entity.Game;
import com.umbral.domain.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameCatalogService {
    private final GameRepository gameRepository;

    public GameCatalogService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public List<GameResponse> getAllGames() {
        List<GameResponse> games = new ArrayList<>();

        for (Game game : gameRepository.findAll()) {
            games.add(new GameResponse(
                    game.getId(),
                    game.getTitle(),
                    game.getDescription()
            ));
        }

        return games;
    }

    public List<CheckpointResponse> getCheckpointsByGameId (Long gameId){

        if (!Long.valueOf(1L).equals(gameId)) {
            return List.of();
        }

        List<CheckpointResponse> checkpoints = new ArrayList<>();
        checkpoints.add(new CheckpointResponse(
                1L,
                "Inicio de la historia",
                1
        ));
        checkpoints.add(new CheckpointResponse(
                2L,
                "Progreso intermedio",
                2
        ));
        checkpoints.add(new CheckpointResponse(
                3L,
                "Progreso avanzado",
                3
        ));
        return checkpoints;
    }
}
