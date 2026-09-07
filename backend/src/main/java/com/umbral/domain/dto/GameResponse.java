package com.umbral.domain.dto;

import java.time.LocalDate;

public record GameResponse(
        Long id,
        String title,
        String description,
        String coverImageUrl,
        LocalDate releaseDate
) {

}
