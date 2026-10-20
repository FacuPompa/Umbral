package com.umbral.domain.dto;

public record TokenResponse(String accessToken, String tokenType, long expiresIn, AuthenticatedUserResponse user) {
    @Override public String toString() { return "TokenResponse[credentials=REDACTED]"; }
}
