package com.umbral.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateGameProgressRequest(
        @NotNull
        @Positive
        Long checkpointId
) {
}