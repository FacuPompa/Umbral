package com.umbral.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateJournalEntryRequest (

        @NotNull
        @Positive
        Long checkpointId,

        @NotBlank
        @Size(max = 5000)
        String content
){
}
