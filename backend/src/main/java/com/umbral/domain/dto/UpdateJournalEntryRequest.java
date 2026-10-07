package com.umbral.domain.dto;

import com.umbral.domain.entity.JournalEntryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateJournalEntryRequest(
        @NotNull JournalEntryType type,
        @NotBlank @Size(max = 5000) String content
) {}
