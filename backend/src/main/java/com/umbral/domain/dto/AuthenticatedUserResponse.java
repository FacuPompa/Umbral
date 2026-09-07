package com.umbral.domain.dto;

import com.umbral.domain.entity.UserRole;

public record AuthenticatedUserResponse(
        Long id,
        String handle,
        String email,
        UserRole role
) {
}
