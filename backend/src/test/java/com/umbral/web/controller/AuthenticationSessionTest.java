package com.umbral.web.controller;

import com.umbral.config.SecurityConfiguration;
import com.umbral.domain.dto.LoginRequest;
import com.umbral.domain.exception.InvalidCredentialsException;
import com.umbral.domain.service.AuthenticationService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthenticationSessionTest {

    private final AuthenticationManager manager = mock(AuthenticationManager.class);
    private final CookieCsrfTokenRepository csrfRepository =
            CookieCsrfTokenRepository.withHttpOnlyFalse();
    private final AuthenticationController controller = new AuthenticationController(
            mock(AuthenticationService.class), manager,
            new HttpSessionSecurityContextRepository(),
            new SecurityConfiguration().sessionAuthenticationStrategy(csrfRepository),
            csrfRepository
    );

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void successfulLoginChangesSessionIdAndPersistsAuthentication() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("XSRF-TOKEN", "pre-login-token"));
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        String previousId = session.getId();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = authenticatedUser();
        when(manager.authenticate(any())).thenReturn(authentication);

        controller.login(new LoginRequest(" test-user ", "test-password"), request, response);

        assertNotEquals(previousId, session.getId());
        SecurityContext savedContext = (SecurityContext) session.getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull(savedContext);
        assertSame(authentication, savedContext.getAuthentication());
        assertEquals(0, response.getCookie("XSRF-TOKEN").getMaxAge());
        verify(manager).authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated("test-user", "test-password"));
    }

    @Test
    void incorrectCredentialsDoNotChangeOrAuthenticateTheSession() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        String previousId = session.getId();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(manager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(InvalidCredentialsException.class, () -> controller.login(
                new LoginRequest("test-user", "incorrect-password"), request, response));

        assertEquals(previousId, session.getId());
        assertNull(session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));
        assertNull(response.getCookie("XSRF-TOKEN"));
    }

    @Test
    void logoutInvalidatesSessionClearsContextAndDeletesCsrfCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        Authentication authentication = authenticatedUser();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertEquals(204, controller.logout(request, response, authentication).getStatusCode().value());

        assertTrue(session.isInvalid());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(0, response.getCookie("XSRF-TOKEN").getMaxAge());
    }

    private Authentication authenticatedUser() {
        return UsernamePasswordAuthenticationToken.authenticated("test-user", null, List.of());
    }
}
