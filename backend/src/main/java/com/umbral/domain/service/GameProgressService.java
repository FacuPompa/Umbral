package com.umbral.domain.service;

import com.umbral.domain.dto.UpdateGameProgressRequest;
import com.umbral.domain.entity.*;
import com.umbral.domain.exception.CheckpointDoesNotBelongToGameException;
import com.umbral.domain.exception.ResourceNotFoundException;
import com.umbral.domain.repository.CheckpointRepository;
import com.umbral.domain.repository.GameRepository;
import com.umbral.domain.repository.UserGameProgressRepository;
import org.springframework.stereotype.Service;

import com.umbral.domain.dto.GameProgressResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GameProgressService {

    private final GameRepository gameRepository;
    private final CheckpointRepository checkpointRepository;
    private final UserGameProgressRepository userGameProgressRepository;
    private final CurrentUserResolver currentUserResolver;
    private final UserGameLibraryService userGameLibraryService;

    public GameProgressService(GameRepository gameRepository, CheckpointRepository checkpointRepository, UserGameProgressRepository userGameProgressRepository, CurrentUserResolver currentUserResolver, UserGameLibraryService userGameLibraryService) {
        this.gameRepository = gameRepository;
        this.checkpointRepository = checkpointRepository;
        this.userGameProgressRepository = userGameProgressRepository;
        this.currentUserResolver = currentUserResolver;
        this.userGameLibraryService = userGameLibraryService;
    }

    @Transactional(readOnly = true)
    public List<GameProgressResponse> getCurrentUserProgress() {
        User currentUser = currentUserResolver.getCurrentUser();
        List<GameProgressResponse> currentUserProgress = new ArrayList<>();
        for (UserGameProgress progress : userGameProgressRepository.findAllByUserId(currentUser.getId())) {
            currentUserProgress.add(toResponse(progress));
        }
        return currentUserProgress;
    }

    @Transactional
    public GameProgressResponse updateCurrentUserProgress(Long gameId, UpdateGameProgressRequest request) {
        User currentUser = currentUserResolver.getCurrentUser();
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El juego no fue encontrado."
                ));
        Checkpoint checkpoint = checkpointRepository.findById(request.checkpointId())
                .orElseThrow(() -> new ResourceNotFoundException("El checkpoint no fue encontrado."));

        if (!checkpoint.getGame().getId().equals(gameId)) {
            throw new CheckpointDoesNotBelongToGameException("El checkpoint no pertenece al juego indicado.");
        }

        Optional<UserGameProgress> existingProgress = userGameProgressRepository.findByUserIdAndGameId(
                currentUser.getId(),
                gameId
        );
        UserGameProgress progress;

        if (existingProgress.isPresent()) {
            progress = existingProgress.get();
            progress.updateCheckpoint(checkpoint);
        } else {
            progress = new UserGameProgress(currentUser, game, checkpoint);
        }

        UserGameProgress savedProgress = userGameProgressRepository.save(progress);

        userGameLibraryService.ensureGameIsInLibraryAsPlaying(currentUser, game);

        return toResponse(savedProgress);
    }

    private GameProgressResponse toResponse(UserGameProgress progress) {
        return new GameProgressResponse(
                progress.getGame().getId(),
                progress.getCheckpoint().getId(),
                progress.getCheckpoint().getLabel(),
                progress.getCheckpoint().getPosition()
        );
    }
}
