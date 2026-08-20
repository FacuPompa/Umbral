package com.umbral.domain.dto;

public record CheckpointResponse(
        Long id,
        String label,
        int position
) {
}
