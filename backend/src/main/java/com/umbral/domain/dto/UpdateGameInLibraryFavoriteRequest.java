package com.umbral.domain.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateGameInLibraryFavoriteRequest(
        @NotNull
        Boolean favorite
) {
}
