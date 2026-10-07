package com.umbral.domain.dto;

import java.util.List;

public record SuggestionPageResponse<T>(List<T> items, int page, int size, boolean hasNext) {}
