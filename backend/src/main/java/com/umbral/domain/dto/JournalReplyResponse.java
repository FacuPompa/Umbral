package com.umbral.domain.dto;

import java.time.Instant;

public record JournalReplyResponse(
        Long id,
        String authorHandle,
        String content,
        Instant createdAt
) {
}
