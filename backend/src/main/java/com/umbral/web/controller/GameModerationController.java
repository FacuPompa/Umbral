package com.umbral.web.controller;

import com.umbral.domain.dto.ApproveGameSuggestionRequest;
import com.umbral.domain.dto.GameSuggestionResponse;
import com.umbral.domain.service.GameSuggestionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/moderation/game-suggestions")
@PreAuthorize("hasRole('MODERATOR')")
public class GameModerationController {

    private final GameSuggestionService gameSuggestionService;

    public GameModerationController(GameSuggestionService gameSuggestionService) {
        this.gameSuggestionService = gameSuggestionService;
    }

    @GetMapping
    public List<GameSuggestionResponse> getPendingSuggestions() {
        return gameSuggestionService.getPendingSuggestionsForModerator();
    }

    @PostMapping("/{suggestionId}/approve")
    public GameSuggestionResponse approve(
            @PathVariable Long suggestionId,
            @Valid @RequestBody ApproveGameSuggestionRequest request
    ) {
        return gameSuggestionService.approveSuggestion(suggestionId, request);
    }

    @PostMapping("/{suggestionId}/reject")
    public GameSuggestionResponse reject(@PathVariable Long suggestionId) {
        return gameSuggestionService.rejectSuggestion(suggestionId);
    }
}
