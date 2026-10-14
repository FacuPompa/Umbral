package com.umbral.domain.service;

import com.umbral.domain.entity.User;
import com.umbral.config.AccountPrincipal;
import com.umbral.domain.exception.UnauthenticatedUserException;
import com.umbral.domain.exception.ResourceNotFoundException;
import com.umbral.domain.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserResolver {

    private final UserRepository userRepository;

    public CurrentUserResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthenticatedUserException("Necesitás iniciar sesión para continuar.");
        }

        User user = userRepository.findByHandle(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el usuario autenticado."
                ));
        long sessionVersion = authentication.getPrincipal() instanceof AccountPrincipal principal
                ? principal.getAuthenticationVersion() : 0;
        if (user.isSuspended() || user.getAuthenticationVersion() != sessionVersion) {
            throw new UnauthenticatedUserException("La cuenta no está habilitada para continuar.");
        }
        return user;
    }
}
