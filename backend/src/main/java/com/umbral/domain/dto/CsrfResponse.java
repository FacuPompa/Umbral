package com.umbral.domain.dto;

public record CsrfResponse(
        String token,
        String headerName
) {
}
