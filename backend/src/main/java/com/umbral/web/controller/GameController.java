package com.umbral.web.controller;

import com.umbral.domain.dto.CheckpointResponse;
import com.umbral.domain.dto.GameResponse;
import com.umbral.domain.service.GameCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {
    private final GameCatalogService gameCatalogService;

    public GameController(GameCatalogService gameCatalogService) {
        this.gameCatalogService = gameCatalogService;
    }

    @GetMapping
    public ResponseEntity<List<GameResponse>> getAllGames(){
        List<GameResponse> games = gameCatalogService.getAllGames();
        return ResponseEntity.ok(games);
    }


    @GetMapping("/{gameId}/checkpoints")
    public ResponseEntity<List<CheckpointResponse>> getCheckpointsByGameId(@PathVariable Long gameId){
        List<CheckpointResponse> checkpoints = gameCatalogService.getCheckpointsByGameId(gameId);
        return ResponseEntity.ok(checkpoints);
    }
}