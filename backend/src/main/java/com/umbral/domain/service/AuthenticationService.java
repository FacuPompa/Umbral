package com.umbral.domain.service;

import com.umbral.domain.dto.AuthenticatedUserResponse;
import com.umbral.domain.dto.RegisterRequest;
import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserRole;
import com.umbral.domain.exception.EmailAlreadyInUseException;
import com.umbral.domain.exception.HandleAlreadyInUseException;
import com.umbral.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String bootstrapModeratorEmail;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${umbral.auth.bootstrap-moderator-email:}") String bootstrapModeratorEmail
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapModeratorEmail = bootstrapModeratorEmail.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public AuthenticatedUserResponse register(RegisterRequest request) {
        String handle = request.handle().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByHandle(handle)) {
            throw new HandleAlreadyInUseException();
        }

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyInUseException();
        }

        UserRole role = shouldBootstrapModerator(email)
                ? UserRole.MODERATOR
                : UserRole.MEMBER;
        User user = new User(handle, email, passwordEncoder.encode(request.password()), role);

        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthenticatedUserResponse getCurrentUser(String handle) {
        User user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new IllegalStateException("El usuario autenticado no existe."));

        return toResponse(user);
    }

    private boolean shouldBootstrapModerator(String email) {
        return !bootstrapModeratorEmail.isBlank()
                && bootstrapModeratorEmail.equals(email)
                && !userRepository.existsByRole(UserRole.MODERATOR);
    }

    private AuthenticatedUserResponse toResponse(User user) {
        return new AuthenticatedUserResponse(
                user.getId(),
                user.getHandle(),
                user.getEmail(),
                user.getRole()
        );
    }
}
