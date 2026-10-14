package com.umbral.config;

import com.umbral.domain.entity.User;
import com.umbral.domain.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/** Registered only in the security chain, after the session context is restored. */
public class ActiveAccountFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;
    private final SecurityContextLogoutHandler logoutHandler;
    private final CsrfLogoutHandler csrfLogoutHandler;

    public ActiveAccountFilter(UserRepository userRepository, SecurityContextRepository contextRepository,
                               CookieCsrfTokenRepository csrfRepository) {
        this.userRepository = userRepository;
        logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.setSecurityContextRepository(contextRepository);
        csrfLogoutHandler = new CsrfLogoutHandler(csrfRepository);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            AccountPrincipal principal = authentication.getPrincipal() instanceof AccountPrincipal account
                    ? account : null;
            Optional<User> currentUser = principal == null
                    ? userRepository.findByHandle(authentication.getName())
                    : userRepository.findById(principal.getUserId());
            // Old sessions predate versioned principals and are valid only at version zero.
            long sessionVersion = principal == null ? 0 : principal.getAuthenticationVersion();
            boolean invalid = currentUser.map(user -> user.isSuspended()
                    || user.getAuthenticationVersion() != sessionVersion).orElse(true);
            if (invalid) {
                csrfLogoutHandler.logout(request, response, authentication);
                logoutHandler.logout(request, response, authentication);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"message\":\"La sesión ya no está disponible. Volvé a iniciar sesión.\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
