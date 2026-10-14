package com.umbral.domain.service;

import com.umbral.domain.dto.CatalogAuditResponse;
import com.umbral.domain.entity.CatalogAuditMetadata;
import com.umbral.domain.entity.UserRole;
import com.umbral.domain.exception.ResourceNotFoundException;
import com.umbral.domain.repository.CheckpointRepository;
import com.umbral.domain.repository.GameRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@PreAuthorize("hasRole('MODERATOR')")
@Transactional(readOnly = true)
public class CatalogAuditService {
    private final GameRepository gameRepository;
    private final CheckpointRepository checkpointRepository;
    private final CurrentUserResolver currentUserResolver;

    public CatalogAuditService(GameRepository gameRepository,
                               CheckpointRepository checkpointRepository,
                               CurrentUserResolver currentUserResolver) {
        this.gameRepository = gameRepository;
        this.checkpointRepository = checkpointRepository;
        this.currentUserResolver = currentUserResolver;
    }

    public CatalogAuditResponse getGameAudit(Long gameId) {
        requireCurrentModerator();
        return toResponse(gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("El juego no fue encontrado.")));
    }

    public CatalogAuditResponse getCheckpointAudit(Long checkpointId) {
        requireCurrentModerator();
        return toResponse(checkpointRepository.findById(checkpointId)
                .orElseThrow(() -> new ResourceNotFoundException("El checkpoint no fue encontrado.")));
    }

    private void requireCurrentModerator() {
        if (currentUserResolver.getCurrentUser().getRole() != UserRole.MODERATOR) {
            throw new AccessDeniedException("Necesitás permisos de moderación para continuar.");
        }
    }

    private CatalogAuditResponse toResponse(CatalogAuditMetadata metadata) {
        return new CatalogAuditResponse(metadata.getCreatedByUserId(), metadata.getCreatedAt(),
                metadata.getUpdatedByUserId(), metadata.getUpdatedAt());
    }
}
