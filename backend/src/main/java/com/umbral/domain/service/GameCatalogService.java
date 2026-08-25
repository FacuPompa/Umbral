package com.umbral.domain.service;

import com.umbral.domain.dto.CheckpointResponse;
import com.umbral.domain.dto.GameResponse;
import com.umbral.domain.entity.Checkpoint;
import com.umbral.domain.entity.Game;
import com.umbral.domain.exception.ResourceNotFoundException;
import com.umbral.domain.repository.CheckpointRepository;
import com.umbral.domain.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameCatalogService {
    private final GameRepository gameRepository;
    private final CheckpointRepository checkpointRepository;

    public GameCatalogService(GameRepository gameRepository, CheckpointRepository checkpointRepository) {
        this.gameRepository = gameRepository;
        this.checkpointRepository = checkpointRepository;
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

    public List<CheckpointResponse> getCheckpointsByGameId(Long gameId) {

        if (!gameRepository.existsById(gameId)) {
            throw new ResourceNotFoundException("El juego no fue encontrado");
        }

        List<CheckpointResponse> checkpoints = new ArrayList<>();

        for (Checkpoint checkpoint : checkpointRepository.findByGameIdOrderByPositionAsc(gameId)) {
            checkpoints.add(new CheckpointResponse(
                    checkpoint.getId(),
                    checkpoint.getLabel(),
                    checkpoint.getPosition()
            ));
        }

        return checkpoints;
    }
}
