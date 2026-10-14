package com.umbral.domain.dto;

import com.umbral.domain.entity.UserRole;
import java.time.Instant;

public record ModeratedUserResponse(Long id, String handle, UserRole role, boolean suspended,
                                    String suspensionReason, String statusChangedByHandle,
                                    Instant statusChangedAt) {
}
