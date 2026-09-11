package com.umbral.domain.dto;

import com.umbral.domain.entity.UserGameLibraryStatus;

import java.time.Instant;

public record GameInLibraryResponse(
        Long gameId,
        String gameTitle,
        String coverImageUrl,
        String checkpointLabel,
        Integer checkpointPosition,
        UserGameLibraryStatus status,
        boolean favorite,
        Instant updatedAt
) {
}
