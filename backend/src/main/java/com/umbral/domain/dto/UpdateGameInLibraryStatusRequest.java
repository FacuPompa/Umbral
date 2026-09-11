package com.umbral.domain.dto;

import com.umbral.domain.entity.UserGameLibraryStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateGameInLibraryStatusRequest(
        @NotNull
        UserGameLibraryStatus status
) {
}
