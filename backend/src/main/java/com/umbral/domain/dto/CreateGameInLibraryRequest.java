package com.umbral.domain.dto;

import com.umbral.domain.entity.UserGameLibraryStatus;
import jakarta.validation.constraints.NotNull;

public record CreateGameInLibraryRequest(
        @NotNull
        Long gameId,
        @NotNull
        UserGameLibraryStatus status
) {
}
