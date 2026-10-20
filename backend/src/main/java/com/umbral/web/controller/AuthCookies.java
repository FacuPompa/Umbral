package com.umbral.web.controller;

import com.umbral.domain.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import java.time.*;

@Component
public class AuthCookies {
    public static final String REFRESH = "umbral-refresh";
    private final boolean secure;
    private final String sameSite;
    public AuthCookies(@Value("${umbral.session-cookie.secure}") boolean secure,
                       @Value("${umbral.session-cookie.same-site}") String sameSite) {
        if ("None".equalsIgnoreCase(sameSite) && !secure)
            throw new IllegalStateException("SameSite=None requiere cookies Secure.");
        this.secure = secure; this.sameSite = sameSite;
    }
    public void set(HttpServletResponse response, RefreshTokenService.Issued issued) {
        cookie(response, issued.refreshToken(), Duration.between(Instant.now(), issued.expiresAt()));
    }
    public void clear(HttpServletResponse response) { cookie(response, "", Duration.ZERO); }
    private void cookie(HttpServletResponse response, String value, Duration age) {
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(REFRESH, value)
                .httpOnly(true).secure(secure).sameSite(sameSite).path("/api/auth")
                .maxAge(age.isNegative() ? Duration.ZERO : age).build().toString());
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
    }
}
