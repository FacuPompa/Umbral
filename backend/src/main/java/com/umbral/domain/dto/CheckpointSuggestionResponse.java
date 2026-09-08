package com.umbral.domain.dto;

import com.umbral.domain.entity.CheckpointSuggestionStatus;

import java.time.Instant;

public record CheckpointSuggestionResponse(
        Long id,
        Long gameId,
        String gameTitle,
        String label,
        int position,
        String suggestedByHandle,
        CheckpointSuggestionStatus status,
        Instant createdAt
) {
}
