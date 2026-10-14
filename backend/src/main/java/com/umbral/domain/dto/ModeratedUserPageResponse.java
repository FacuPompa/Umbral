package com.umbral.domain.dto;

import java.util.List;

public record ModeratedUserPageResponse(List<ModeratedUserResponse> items, int page, int size, boolean hasNext) {
}
