package com.umbral.web.controller;

import com.umbral.domain.dto.ChangePasswordRequest;
import com.umbral.domain.service.PasswordChangeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfLogoutHandler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me/password")
public class PasswordChangeController {
    private final PasswordChangeService service;
    private final SecurityContextLogoutHandler logoutHandler;
    private final CsrfLogoutHandler csrfLogoutHandler;

    public PasswordChangeController(PasswordChangeService service, SecurityContextRepository contextRepository,
                                    CookieCsrfTokenRepository csrfRepository) {
        this.service = service;
        logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.setSecurityContextRepository(contextRepository);
        csrfLogoutHandler = new CsrfLogoutHandler(csrfRepository);
    }

    @PostMapping
    public ResponseEntity<Void> change(@Valid @RequestBody ChangePasswordRequest body,
                                       HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) {
        service.changeCurrentUserPassword(body);
        csrfLogoutHandler.logout(request, response, authentication);
        logoutHandler.logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }
}
