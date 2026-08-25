package com.umbral.domain.dto;

public record GameProgressResponse(
        Long gameId,
        Long checkpointId,
        String checkpointLabel,
        int position
) {
}
