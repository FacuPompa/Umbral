package com.umbral.domain.dto;

import com.umbral.domain.entity.JournalEntryType;

import java.time.Instant;

public record JournalEntryResponse(
        Long id,
        String authorHandle,
        Long gameId,
        String checkpointLabel,
        JournalEntryType type,
        String content,
        Instant createdAt
) {
}
