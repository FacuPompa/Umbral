package com.umbral.domain.dto;

import java.time.Instant;

public record CatalogAuditResponse(
        Long createdByUserId,
        Instant createdAt,
        Long updatedByUserId,
        Instant updatedAt
) {
}
