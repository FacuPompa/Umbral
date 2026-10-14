package com.umbral.domain.service;

import com.umbral.domain.dto.ChangePasswordRequest;
import com.umbral.domain.entity.User;
import com.umbral.domain.exception.InvalidPasswordChangeException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;

@Service
public class PasswordChangeService {
    private final CurrentUserResolver currentUserResolver;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    public PasswordChangeService(CurrentUserResolver currentUserResolver, PasswordEncoder passwordEncoder,
                                 EntityManager entityManager) {
        this.currentUserResolver = currentUserResolver;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void changeCurrentUserPassword(ChangePasswordRequest request) {
        User user = currentUserResolver.getCurrentUser();
        // Refresh under the moderation row lock and recheck concurrent revocation.
        entityManager.refresh(user, LockModeType.PESSIMISTIC_WRITE);
        currentUserResolver.getCurrentUser();
        if (request.newPassword().length() < 8 || request.newPassword().isBlank()
                || request.newPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new InvalidPasswordChangeException("La nueva contraseña debe tener al menos 8 caracteres y no superar 72 bytes.");
        }
        if (request.currentPassword().getBytes(StandardCharsets.UTF_8).length > 72
                || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordChangeException("La contraseña actual no es correcta.");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordChangeException("Elegí una contraseña diferente de la actual.");
        }
        user.changePassword(passwordEncoder.encode(request.newPassword()));
    }
}
