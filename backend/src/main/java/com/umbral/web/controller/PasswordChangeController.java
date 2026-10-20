package com.umbral.web.controller;

import com.umbral.domain.dto.ChangePasswordRequest;
import com.umbral.domain.service.PasswordChangeService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me/password")
public class PasswordChangeController {
    private final PasswordChangeService service;
    private final AuthCookies cookies;
    private final CsrfLogoutHandler csrfLogout;
    public PasswordChangeController(PasswordChangeService service, AuthCookies cookies, CookieCsrfTokenRepository csrf) {
        this.service = service; this.cookies = cookies; csrfLogout = new CsrfLogoutHandler(csrf);
    }
    @PostMapping
    public ResponseEntity<Void> change(@Valid @RequestBody ChangePasswordRequest body, HttpServletRequest request,
                                       HttpServletResponse response, Authentication authentication) {
        service.changeCurrentUserPassword(body);
        cookies.clear(response);
        csrfLogout.logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }
}
