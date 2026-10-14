package com.umbral.domain.service;

import com.umbral.domain.dto.*;
import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserRole;
import com.umbral.domain.exception.InvalidAccountModerationException;
import com.umbral.domain.exception.ResourceNotFoundException;
import com.umbral.domain.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@PreAuthorize("hasRole('MODERATOR')")
public class UserModerationService {
    private final UserRepository userRepository;
    private final CurrentUserResolver currentUserResolver;

    public UserModerationService(UserRepository userRepository, CurrentUserResolver currentUserResolver) {
        this.userRepository = userRepository;
        this.currentUserResolver = currentUserResolver;
    }

    @Transactional(readOnly = true)
    public ModeratedUserPageResponse search(String query, Boolean suspended, int page, int size) {
        requireModerator();
        PageRequest pageable = PageRequest.of(page, size);
        Slice<User> users = suspended == null
                ? userRepository.findByHandleContainingIgnoreCaseOrderByHandleAscIdAsc(query.trim(), pageable)
                : userRepository.findBySuspendedAndHandleContainingIgnoreCaseOrderByHandleAscIdAsc(
                        suspended, query.trim(), pageable);
        return new ModeratedUserPageResponse(users.map(this::toResponse).getContent(), page, size, users.hasNext());
    }

    @Transactional
    public ModeratedUserResponse suspend(Long userId, SuspendUserRequest request) {
        User moderator = requireModerator();
        User target = findAllowedTarget(userId, moderator);
        String reason = request.reason().trim();
        if (reason.length() < 3 || reason.length() > 500) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "El motivo debe tener entre 3 y 500 caracteres.");
        }
        target.suspend(reason, moderator);
        return toResponse(target);
    }

    @Transactional
    public ModeratedUserResponse reactivate(Long userId) {
        User moderator = requireModerator();
        User target = findAllowedTarget(userId, moderator);
        target.reactivate(moderator);
        return toResponse(target);
    }

    private User requireModerator() {
        User moderator = currentUserResolver.getCurrentUser();
        if (moderator.getRole() != UserRole.MODERATOR) {
            throw new AccessDeniedException("Necesitás permisos de moderación para continuar.");
        }
        return moderator;
    }

    private User findAllowedTarget(Long userId, User moderator) {
        User target = userRepository.findForAccountModeration(userId)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario no fue encontrado."));
        if (target.getId().equals(moderator.getId()) || target.getRole() == UserRole.MODERATOR) {
            throw new InvalidAccountModerationException();
        }
        return target;
    }

    private ModeratedUserResponse toResponse(User user) {
        User actor = user.getAccountStatusChangedBy();
        return new ModeratedUserResponse(user.getId(), user.getHandle(), user.getRole(), user.isSuspended(),
                user.getSuspensionReason(), actor == null ? null : actor.getHandle(), user.getAccountStatusChangedAt());
    }
}
