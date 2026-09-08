package com.umbral.web.controller;

import com.umbral.domain.dto.CheckpointSuggestionResponse;
import com.umbral.domain.service.CheckpointSuggestionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/moderation/checkpoint-suggestions")
@PreAuthorize("hasRole('MODERATOR')")
public class CheckpointModerationController {

    private final CheckpointSuggestionService checkpointSuggestionService;

    public CheckpointModerationController(CheckpointSuggestionService checkpointSuggestionService) {
        this.checkpointSuggestionService = checkpointSuggestionService;
    }

    @GetMapping
    public List<CheckpointSuggestionResponse> getPendingSuggestions() {
        return checkpointSuggestionService.getPendingSuggestionsForModerator();
    }

    @PostMapping("/{suggestionId}/approve")
    public CheckpointSuggestionResponse approve(@PathVariable Long suggestionId) {
        return checkpointSuggestionService.approveSuggestion(suggestionId);
    }

    @PostMapping("/{suggestionId}/reject")
    public CheckpointSuggestionResponse reject(@PathVariable Long suggestionId) {
        return checkpointSuggestionService.rejectSuggestion(suggestionId);
    }
}
