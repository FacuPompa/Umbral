package com.umbral.web.controller;

import com.umbral.domain.dto.GameResponse;
import com.umbral.domain.service.GameCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
