package com.umbral.config;

import com.umbral.domain.entity.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serial;
import java.util.List;

/** Identity and revocation version verified for the current request. */
public class AccountPrincipal extends org.springframework.security.core.userdetails.User {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Long userId;
    private final long authenticationVersion;
    private final java.util.UUID grantId;

    public AccountPrincipal(User user) {
        this(user, null);
    }

    public AccountPrincipal(User user, java.util.UUID grantId) {
        super(user.getHandle(), user.getPasswordHash(), !user.isSuspended(), true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        userId = user.getId();
        authenticationVersion = user.getAuthenticationVersion();
        this.grantId = grantId;
    }

    public Long getUserId() { return userId; }
    public long getAuthenticationVersion() { return authenticationVersion; }
    public java.util.UUID getGrantId() { return grantId; }
}
