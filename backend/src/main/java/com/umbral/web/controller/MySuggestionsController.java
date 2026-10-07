package com.umbral.web.controller;

import com.umbral.domain.dto.*;
import com.umbral.domain.entity.CheckpointSuggestionStatus;
import com.umbral.domain.entity.GameSuggestionStatus;
import com.umbral.domain.service.CheckpointSuggestionService;
import com.umbral.domain.service.GameSuggestionService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me/suggestions")
public class MySuggestionsController {
    private final GameSuggestionService games;
    private final CheckpointSuggestionService checkpoints;

    public MySuggestionsController(GameSuggestionService games, CheckpointSuggestionService checkpoints) {
        this.games = games;
        this.checkpoints = checkpoints;
    }

    @GetMapping("/games")
    public SuggestionPageResponse<GameSuggestionResponse> games(
            @RequestParam(required = false) GameSuggestionStatus status,
            @RequestParam(defaultValue = "0") @Min(0) @Max(10000) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size
    ) {
        return games.getCurrentUserSuggestions(status, page, size);
    }

    @GetMapping("/checkpoints")
    public SuggestionPageResponse<CheckpointSuggestionResponse> checkpoints(
            @RequestParam(required = false) CheckpointSuggestionStatus status,
            @RequestParam(defaultValue = "0") @Min(0) @Max(10000) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size
    ) {
        return checkpoints.getCurrentUserSuggestions(status, page, size);
    }
}
