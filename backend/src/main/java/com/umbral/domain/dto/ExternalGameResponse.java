package com.umbral.domain.dto;

import java.time.LocalDate;

public record ExternalGameResponse(
        Long rawgGameId,
        String title,
        LocalDate releaseDate,
        String coverImageUrl
) {
}
