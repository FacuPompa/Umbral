package com.umbral.domain.dto;

public record PublicProfileFavoriteGameResponse(
        Long gameId,
        String gameTitle,
        String coverImageUrl
) {
}
