package com.umbral.web.controller;

import com.umbral.config.AccountPrincipal;
import com.umbral.domain.dto.*;
import com.umbral.domain.exception.InvalidCredentialsException;
import com.umbral.domain.service.*;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.web.csrf.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService users;
    private final AuthenticationManager manager;
    private final RefreshTokenService tokens;
    private final AuthCookies cookies;
    private final CsrfLogoutHandler csrfLogout;
    public AuthenticationController(AuthenticationService users, AuthenticationManager manager, RefreshTokenService tokens,
                                    AuthCookies cookies, CookieCsrfTokenRepository csrf) {
        this.users = users; this.manager = manager; this.tokens = tokens; this.cookies = cookies;
        csrfLogout = new CsrfLogoutHandler(csrf);
    }
    @GetMapping("/csrf")
    public ResponseEntity<CsrfResponse> csrf(CsrfToken token) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new CsrfResponse(token.getToken(), token.getHeaderName()));
    }
    @PostMapping("/register")
    public ResponseEntity<AuthenticatedUserResponse> register(@Valid @RequestBody RegisterRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(users.register(body));
    }
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest body, HttpServletRequest request, HttpServletResponse response) {
        try {
            var auth = manager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(body.handle().trim(), body.password()));
            try {
                // Replace the browser's previous login grant rather than leaving a live orphan.
                tokens.logout(refreshCookie(request), null);
                var issued = tokens.login((AccountPrincipal) auth.getPrincipal());
                cookies.set(response, issued);
                csrfLogout.logout(request, response, auth);
                return issued.response();
            } finally { if (auth instanceof CredentialsContainer credentials) credentials.eraseCredentials(); }
        } catch (AuthenticationException e) { throw new InvalidCredentialsException(); }
    }
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        var issued = tokens.refresh(refreshCookie(request));
        if (issued.isEmpty()) {
            cookies.clear(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        cookies.set(response, issued.get());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(issued.get().response());
    }
    @GetMapping("/me")
    public AuthenticatedUserResponse me(Authentication auth) { return users.getCurrentUser(auth.getName()); }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
        tokens.logout(refreshCookie(request), auth != null && auth.getPrincipal() instanceof AccountPrincipal p ? p.getGrantId() : null);
        cookies.clear(response);
        csrfLogout.logout(request, response, auth);
        return ResponseEntity.noContent().build();
    }
    private String refreshCookie(HttpServletRequest request) {
        if (request.getCookies() != null)
            for (var cookie : request.getCookies()) if (AuthCookies.REFRESH.equals(cookie.getName())) return cookie.getValue();
        return null;
    }
}
