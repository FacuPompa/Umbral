package com.umbral.domain.dto;

import java.util.List;

public record SearchResponse(
        List<GameResponse> games,
        List<PublicUserSearchResponse> users
) {
}
