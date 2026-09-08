package com.umbral.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateCheckpointSuggestionRequest(
        @NotBlank @Size(max = 255) String label,
        @Positive int position
) {
}
