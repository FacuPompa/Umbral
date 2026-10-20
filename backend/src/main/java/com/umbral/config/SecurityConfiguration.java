package com.umbral.config;

import com.umbral.domain.repository.UserRepository;
import com.umbral.domain.service.JwtTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.security.web.csrf.*;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.web.cors.*;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, CookieCsrfTokenRepository csrf,
                                            JwtTokenService jwt, UserRepository users) throws Exception {
        return http.cors(Customizer.withDefaults())
                .csrf(config -> config.csrfTokenRepository(csrf).requireCsrfProtectionMatcher(request -> {
                    if (!CsrfFilter.DEFAULT_CSRF_MATCHER.matches(request)) return false;
                    // Cookie-authenticated auth routes always require CSRF. A Bearer-only API call does not.
                    String path = request.getServletPath().isEmpty() ? request.getRequestURI() : request.getServletPath();
                    return path.startsWith("/api/auth/") || request.getHeader(HttpHeaders.AUTHORIZATION) == null;
                }))
                .securityContext(c -> c.securityContextRepository(new NullSecurityContextRepository()))
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(c -> c
                        // Spring exempts Bearer requests from CSRF by default. Auth cookie routes must never use that exemption.
                        .bearerTokenResolver(request -> {
                            String path = request.getRequestURI();
                            if (List.of("/api/auth/login", "/api/auth/register", "/api/auth/refresh",
                                    "/api/auth/logout", "/api/auth/csrf").contains(path)) return null;
                            return new DefaultBearerTokenResolver().resolve(request);
                        })
                        .jwt(j -> j.jwtAuthenticationConverter(jwt::authenticate)))
                .addFilterAfter(new ActiveAccountFilter(users), BearerTokenAuthenticationFilter.class)
                .authorizeHttpRequests(c -> c
                        .requestMatchers("/api/auth/csrf", "/api/auth/register", "/api/auth/login",
                                "/api/auth/refresh", "/api/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/games", "/api/games/*/checkpoints", "/api/users/*", "/api/search").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(c -> c.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .formLogin(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable).build();
    }
    @Bean
    CookieCsrfTokenRepository csrfTokenRepository(@Value("${umbral.session-cookie.same-site}") String sameSite,
                                                 @Value("${umbral.session-cookie.secure}") boolean secure) {
        var repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieCustomizer(cookie -> cookie.sameSite(sameSite).secure(secure).path("/api"));
        return repository;
    }
    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${umbral.cors.allowed-origin:http://localhost:5173}") String allowedOrigin) {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigin));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(HttpHeaders.CONTENT_TYPE, HttpHeaders.AUTHORIZATION, "X-XSRF-TOKEN"));
        config.setAllowCredentials(true);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean AuthenticationManager authenticationManager(UserDetailsService users, PasswordEncoder encoder) {
        var provider = new DaoAuthenticationProvider(users); provider.setPasswordEncoder(encoder);
        return provider::authenticate;
    }
}
