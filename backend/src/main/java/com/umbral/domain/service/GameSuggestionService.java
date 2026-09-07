package com.umbral.domain.service;

import com.umbral.domain.dto.*;
import com.umbral.domain.entity.Game;
import com.umbral.domain.entity.GameSuggestion;
import com.umbral.domain.entity.GameSuggestionStatus;
import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserRole;
import com.umbral.domain.exception.GameAlreadyInCatalogException;
import com.umbral.domain.exception.GameSuggestionAlreadyExistsException;
import com.umbral.domain.exception.ResourceNotFoundException;
import com.umbral.domain.exception.SuggestionAlreadyReviewedException;
import com.umbral.domain.repository.GameRepository;
import com.umbral.domain.repository.GameSuggestionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GameSuggestionService {

    private final GameDiscoveryClient gameDiscoveryClient;
    private final GameSuggestionRepository gameSuggestionRepository;
    private final GameRepository gameRepository;
    private final CurrentUserResolver currentUserResolver;

    public GameSuggestionService(
            GameDiscoveryClient gameDiscoveryClient,
            GameSuggestionRepository gameSuggestionRepository,
            GameRepository gameRepository,
            CurrentUserResolver currentUserResolver
    ) {
        this.gameDiscoveryClient = gameDiscoveryClient;
        this.gameSuggestionRepository = gameSuggestionRepository;
        this.gameRepository = gameRepository;
        this.currentUserResolver = currentUserResolver;
    }

    @Transactional(readOnly = true)
    public List<ExternalGameResponse> searchExternalGames(String query) {
        return gameDiscoveryClient.search(query.trim());
    }

    @Transactional
    public GameSuggestionResponse createCurrentUserSuggestion(CreateGameSuggestionRequest request) {
        User suggestedBy = currentUserResolver.getCurrentUser();
        Long rawgGameId = request.rawgGameId();

        if (gameRepository.existsByRawgGameId(rawgGameId)) {
            throw new GameAlreadyInCatalogException();
        }

        if (gameSuggestionRepository.existsByRawgGameId(rawgGameId)) {
            throw new GameSuggestionAlreadyExistsException();
        }

        ExternalGameResponse externalGame = gameDiscoveryClient.findById(rawgGameId);

        if (gameRepository.existsByTitle(externalGame.title())) {
            throw new GameAlreadyInCatalogException();
        }

        GameSuggestion suggestion = new GameSuggestion(
                externalGame.rawgGameId(),
                externalGame.title(),
                externalGame.releaseDate(),
                externalGame.coverImageUrl(),
                suggestedBy
        );

        return toResponse(gameSuggestionRepository.save(suggestion));
    }

    @Transactional(readOnly = true)
    public List<GameSuggestionResponse> getPendingSuggestionsForModerator() {
        requireModerator();

        return gameSuggestionRepository
                .findAllByStatusOrderByCreatedAtAscIdAsc(GameSuggestionStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public GameSuggestionResponse approveSuggestion(Long suggestionId, ApproveGameSuggestionRequest request) {
        User moderator = requireModerator();
        GameSuggestion suggestion = findPendingSuggestion(suggestionId);

        if (gameRepository.existsByRawgGameId(suggestion.getRawgGameId())
                || gameRepository.existsByTitle(suggestion.getTitle())) {
            throw new GameAlreadyInCatalogException();
        }

        gameRepository.save(new Game(
                suggestion.getTitle(),
                request.safeDescription().trim(),
                suggestion.getRawgGameId(),
                suggestion.getCoverImageUrl(),
                suggestion.getReleaseDate()
        ));
        suggestion.approve(moderator);

        return toResponse(suggestion);
    }

    @Transactional
    public GameSuggestionResponse rejectSuggestion(Long suggestionId) {
        User moderator = requireModerator();
        GameSuggestion suggestion = findPendingSuggestion(suggestionId);
        suggestion.reject(moderator);

        return toResponse(suggestion);
    }

    private GameSuggestion findPendingSuggestion(Long suggestionId) {
        GameSuggestion suggestion = gameSuggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new ResourceNotFoundException("La sugerencia no fue encontrada."));

        if (suggestion.getStatus() != GameSuggestionStatus.PENDING) {
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

    private GameSuggestionResponse toResponse(GameSuggestion suggestion) {
        return new GameSuggestionResponse(
                suggestion.getId(),
                suggestion.getRawgGameId(),
                suggestion.getTitle(),
                suggestion.getReleaseDate(),
                suggestion.getCoverImageUrl(),
                suggestion.getSuggestedBy().getHandle(),
                suggestion.getStatus(),
                suggestion.getCreatedAt()
        );
    }
}
