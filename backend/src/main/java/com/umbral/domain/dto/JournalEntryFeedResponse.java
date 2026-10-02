package com.umbral.domain.dto;

import java.util.List;

public record JournalEntryFeedResponse(
        List<JournalEntryResponse> items,
        int page,
        int size,
        boolean hasNext
) {
}
