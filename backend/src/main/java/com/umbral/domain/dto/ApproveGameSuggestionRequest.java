package com.umbral.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApproveGameSuggestionRequest(
        @NotBlank
        @Size(max = 255)
        String safeDescription
) {
}
