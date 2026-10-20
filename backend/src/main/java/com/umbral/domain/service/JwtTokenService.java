package com.umbral.domain.service;

import com.umbral.config.AccountPrincipal;
import com.umbral.domain.entity.RefreshGrant;
import com.umbral.domain.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
public class JwtTokenService {
    public static final long ACCESS_SECONDS = 600;
    private final JwtEncoder encoder;
    private final UserRepository users;
    private final RefreshGrantRepository grants;
    private final String issuer;
    public JwtTokenService(JwtEncoder encoder, UserRepository users, RefreshGrantRepository grants,
                           @Value("${umbral.jwt.issuer}") String issuer) {
        this.encoder = encoder; this.users = users; this.grants = grants; this.issuer = issuer;
    }
    public String issue(RefreshGrant grant) {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer(issuer).audience(List.of("umbral-api"))
                .subject(grant.getUser().getId().toString()).issuedAt(now).expiresAt(now.plusSeconds(ACCESS_SECONDS))
                .id(UUID.randomUUID().toString()).claim("kind", "access")
                .claim("ver", grant.getAuthenticationVersion()).claim("sid", grant.getId().toString()).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
    @Transactional(readOnly = true)
    public AbstractAuthenticationToken authenticate(Jwt jwt) {
        try {
            var user = users.findById(Long.valueOf(jwt.getSubject())).orElseThrow();
            var grant = grants.findById(UUID.fromString(jwt.getClaimAsString("sid"))).orElseThrow();
            Number version = jwt.getClaim("ver");
            if (version == null || user.isSuspended() || user.getAuthenticationVersion() != version.longValue()
                    || !grant.getUser().getId().equals(user.getId()) || grant.isRevoked()
                    || !grant.getExpiresAt().isAfter(Instant.now())
                    || grant.getAuthenticationVersion() != version.longValue()) throw new IllegalArgumentException();
            var principal = new AccountPrincipal(user, grant.getId());
            principal.eraseCredentials();
            return UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
        } catch (IllegalArgumentException | NoSuchElementException | ClassCastException e) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token"));
        }
    }
}
