package com.umbral.config;

import com.umbral.domain.entity.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serial;
import java.util.List;

/** Identity and revocation version captured at login, persisted in the HTTP session. */
public class AccountPrincipal extends org.springframework.security.core.userdetails.User {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Long userId;
    private final long authenticationVersion;

    public AccountPrincipal(User user) {
        super(user.getHandle(), user.getPasswordHash(), !user.isSuspended(), true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        userId = user.getId();
        authenticationVersion = user.getAuthenticationVersion();
    }

    public Long getUserId() { return userId; }
    public long getAuthenticationVersion() { return authenticationVersion; }
}
