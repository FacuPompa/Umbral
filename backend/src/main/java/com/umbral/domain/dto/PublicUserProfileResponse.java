package com.umbral.domain.dto;

import java.util.List;

public record PublicUserProfileResponse(
        String handle,
        long libraryCount,
        long completedCount,
        long favoriteCount,
        List<PublicProfileFavoriteGameResponse> favoriteGames
) {
}
