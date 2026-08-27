package com.umbral.domain.dto;

import java.time.Instant;

public record JournalEntryResponse(
        Long id,
        String authorHandle,
        Long gameId,
        String checkpointLabel,
        String content,
        Instant createdAt
) {
}
