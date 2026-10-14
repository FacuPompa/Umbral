package com.umbral.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SuspendUserRequest(@NotBlank @Size(min = 3, max = 500) String reason) {
}
