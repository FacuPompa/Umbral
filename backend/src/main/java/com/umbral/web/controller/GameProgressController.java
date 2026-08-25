package com.umbral.web.controller;

import com.umbral.domain.dto.GameProgressResponse;
import com.umbral.domain.dto.UpdateGameProgressRequest;
import com.umbral.domain.service.GameProgressService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/me")
public class GameProgressController {
    private final GameProgressService gameProgressService;

    public GameProgressController(GameProgressService gameProgressService) {
        this.gameProgressService = gameProgressService;
    }

    @GetMapping("/game-progress")
    public ResponseEntity<List<GameProgressResponse>> getGameProgress() {
        List<GameProgressResponse> gameProgress = gameProgressService.getCurrentUserProgress();

        return ResponseEntity.ok(gameProgress);
    }

    @PutMapping("/games/{gameId}/progress")
    public ResponseEntity<GameProgressResponse> putGameProgress(
            @PathVariable Long gameId,
            @Valid @RequestBody UpdateGameProgressRequest request
    ) {
        GameProgressResponse gameProgress = gameProgressService.updateCurrentUserProgress(gameId, request);

        return ResponseEntity.ok(gameProgress);
    }
}
