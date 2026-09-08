package com.umbral.web.controller;

import com.umbral.domain.dto.CheckpointSuggestionResponse;
import com.umbral.domain.dto.CreateCheckpointSuggestionRequest;
import com.umbral.domain.service.CheckpointSuggestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games/{gameId}/checkpoint-suggestions")
public class CheckpointSuggestionController {

    private final CheckpointSuggestionService checkpointSuggestionService;

    public CheckpointSuggestionController(CheckpointSuggestionService checkpointSuggestionService) {
        this.checkpointSuggestionService = checkpointSuggestionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CheckpointSuggestionResponse create(
            @PathVariable Long gameId,
            @Valid @RequestBody CreateCheckpointSuggestionRequest request
    ) {
        return checkpointSuggestionService.createCurrentUserSuggestion(gameId, request);
    }
}
