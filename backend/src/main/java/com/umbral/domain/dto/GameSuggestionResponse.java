package com.umbral.domain.dto;

import com.umbral.domain.entity.GameSuggestionStatus;

import java.time.Instant;
import java.time.LocalDate;

public record GameSuggestionResponse(
        Long id,
        Long rawgGameId,
        String title,
        LocalDate releaseDate,
        String coverImageUrl,
        String suggestedByHandle,
        GameSuggestionStatus status,
        Instant createdAt
) {
}
