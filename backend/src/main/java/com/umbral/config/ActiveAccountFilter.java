package com.umbral.config;

import com.umbral.domain.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/** Defense in depth for identities supplied by trusted internal/test authentication. JWTs are checked by the converter. */
public class ActiveAccountFilter extends OncePerRequestFilter {
    private final UserRepository users;
    public ActiveAccountFilter(UserRepository users) { this.users = users; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            long version = auth.getPrincipal() instanceof AccountPrincipal p ? p.getAuthenticationVersion() : 0;
            boolean invalid = users.findByHandle(auth.getName())
                    .map(u -> u.isSuspended() || u.getAuthenticationVersion() != version).orElse(true);
            if (invalid) {
                SecurityContextHolder.clearContext();
                response.setStatus(401);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
