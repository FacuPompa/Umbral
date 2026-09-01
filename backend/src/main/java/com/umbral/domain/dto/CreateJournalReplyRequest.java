package com.umbral.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateJournalReplyRequest(
        @NotBlank
        @Size(max = 5000)
        String content
) {
}