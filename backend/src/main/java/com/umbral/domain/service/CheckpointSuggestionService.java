package com.umbral.domain.service;

import com.umbral.domain.dto.CheckpointSuggestionResponse;
import com.umbral.domain.dto.CreateCheckpointSuggestionRequest;
import com.umbral.domain.entity.*;
import com.umbral.domain.exception.CheckpointPositionAlreadyExistsException;
import com.umbral.domain.exception.CheckpointSuggestionAlreadyExistsException;
import com.umbral.domain.exception.ResourceNotFoundException;
import com.umbral.domain.exception.SuggestionAlreadyReviewedException;
import com.umbral.domain.repository.CheckpointRepository;
import com.umbral.domain.repository.CheckpointSuggestionRepository;
import com.umbral.domain.repository.GameRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CheckpointSuggestionService {

    private final CheckpointSuggestionRepository checkpointSuggestionRepository;
    private final CheckpointRepository checkpointRepository;
    private final GameRepository gameRepository;
    private final CurrentUserResolver currentUserResolver;

    public CheckpointSuggestionService(
            CheckpointSuggestionRepository checkpointSuggestionRepository,
            CheckpointRepository checkpointRepository,
            GameRepository gameRepository,
            CurrentUserResolver currentUserResolver
    ) {
        this.checkpointSuggestionRepository = checkpointSuggestionRepository;
        this.checkpointRepository = checkpointRepository;
        this.gameRepository = gameRepository;
        this.currentUserResolver = currentUserResolver;
    }

    @Transactional
    public CheckpointSuggestionResponse createCurrentUserSuggestion(
            Long gameId,
            CreateCheckpointSuggestionRequest request
    ) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("El juego no fue encontrado."));

        if (checkpointRepository.existsByGameIdAndPosition(gameId, request.position())) {
            throw new CheckpointPositionAlreadyExistsException();
        }

        if (checkpointSuggestionRepository.existsByGameIdAndPositionAndStatus(
                gameId,
                request.position(),
                CheckpointSuggestionStatus.PENDING
        )) {
            throw new CheckpointSuggestionAlreadyExistsException();
        }

        CheckpointSuggestion suggestion = new CheckpointSuggestion(
                game,
                request.label().trim(),
                request.position(),
                currentUserResolver.getCurrentUser()
        );

        return toResponse(checkpointSuggestionRepository.save(suggestion));
    }

    @Transactional(readOnly = true)
    public List<CheckpointSuggestionResponse> getPendingSuggestionsForModerator() {
        requireModerator();

        return checkpointSuggestionRepository
                .findAllByStatusOrderByCreatedAtAscIdAsc(CheckpointSuggestionStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CheckpointSuggestionResponse approveSuggestion(Long suggestionId) {
        User moderator = requireModerator();
        CheckpointSuggestion suggestion = findPendingSuggestion(suggestionId);

        if (checkpointRepository.existsByGameIdAndPosition(
                suggestion.getGame().getId(),
                suggestion.getPosition()
        )) {
            throw new CheckpointPositionAlreadyExistsException();
        }

        checkpointRepository.save(new Checkpoint(
                suggestion.getGame(),
                suggestion.getLabel(),
                suggestion.getPosition()
        ));
        suggestion.approve(moderator);

        return toResponse(suggestion);
    }

    @Transactional
    public CheckpointSuggestionResponse rejectSuggestion(Long suggestionId) {
        User moderator = requireModerator();
        CheckpointSuggestion suggestion = findPendingSuggestion(suggestionId);
        suggestion.reject(moderator);

        return toResponse(suggestion);
    }

    private CheckpointSuggestion findPendingSuggestion(Long suggestionId) {
        CheckpointSuggestion suggestion = checkpointSuggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new ResourceNotFoundException("La sugerencia de checkpoint no fue encontrada."));

        if (suggestion.getStatus() != CheckpointSuggestionStatus.PENDING) {
            throw new SuggestionAlreadyReviewedException();
        }

        return suggestion;
    }

    private User requireModerator() {
        User currentUser = currentUserResolver.getCurrentUser();

        if (currentUser.getRole() != UserRole.MODERATOR) {
            throw new AccessDeniedException("Necesitás permisos de moderación para continuar.");
        }

        return currentUser;
    }

    private CheckpointSuggestionResponse toResponse(CheckpointSuggestion suggestion) {
        return new CheckpointSuggestionResponse(
                suggestion.getId(),
                suggestion.getGame().getId(),
                suggestion.getGame().getTitle(),
                suggestion.getLabel(),
                suggestion.getPosition(),
                suggestion.getSuggestedBy().getHandle(),
                suggestion.getStatus(),
                suggestion.getCreatedAt()
        );
    }
}
