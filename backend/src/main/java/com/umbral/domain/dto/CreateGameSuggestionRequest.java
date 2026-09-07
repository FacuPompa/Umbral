package com.umbral.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateGameSuggestionRequest(
        @NotNull
        @Positive
        Long rawgGameId
) {
}
