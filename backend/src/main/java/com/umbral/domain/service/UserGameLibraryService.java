package com.umbral.domain.service;

import com.umbral.domain.dto.CreateGameInLibraryRequest;
import com.umbral.domain.dto.GameInLibraryResponse;
import com.umbral.domain.dto.UpdateGameInLibraryFavoriteRequest;
import com.umbral.domain.dto.UpdateGameInLibraryStatusRequest;
import com.umbral.domain.entity.*;
import com.umbral.domain.exception.GameAlreadyInLibraryException;
import com.umbral.domain.exception.ResourceNotFoundException;
import com.umbral.domain.repository.GameRepository;
import com.umbral.domain.repository.UserGameLibraryRepository;
import com.umbral.domain.repository.UserGameProgressRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserGameLibraryService {
    private final UserGameLibraryRepository userGameLibraryRepository;
    private final GameRepository gameRepository;
    private final UserGameProgressRepository userGameProgressRepository;
    private final CurrentUserResolver currentUserResolver;


    public UserGameLibraryService(UserGameLibraryRepository userGameLibraryRepository, GameRepository gameRepository, UserGameProgressRepository userGameProgressRepository, CurrentUserResolver currentUserResolver) {
        this.userGameLibraryRepository = userGameLibraryRepository;
        this.gameRepository = gameRepository;
        this.userGameProgressRepository = userGameProgressRepository;
        this.currentUserResolver = currentUserResolver;
    }

    @Transactional(readOnly = true)
    public List<GameInLibraryResponse> getCurrentUserLibrary() {
        User currentUser = currentUserResolver.getCurrentUser();
        List<UserGameLibrary> libraryEntries = userGameLibraryRepository.findAllByUserIdOrderByUpdatedAtDesc(currentUser.getId());
        List<UserGameProgress> progresses = userGameProgressRepository.findAllByUserId(currentUser.getId());
        Map<Long, UserGameProgress> progressByGameId = new HashMap<>();

        for (UserGameProgress progress : progresses) {
            progressByGameId.put(progress.getGame().getId(), progress);
        }
        List<GameInLibraryResponse> responses = new ArrayList<>();

        for (UserGameLibrary libraryEntry: libraryEntries) {
            UserGameProgress progress = progressByGameId.get(libraryEntry.getGame().getId());

            GameInLibraryResponse response = toResponse(libraryEntry, progress);
            responses.add(response);
        }

        return responses;
    }

    @Transactional
    public GameInLibraryResponse addCurrentUserGame (CreateGameInLibraryRequest request) {
        User currentUser = currentUserResolver.getCurrentUser();
        Game game = gameRepository.findById(request.gameId()).orElseThrow(() -> new ResourceNotFoundException(
                "El juego no fue encontrado"
        ));

        if (userGameLibraryRepository.findByUserIdAndGameId(currentUser.getId(), game.getId()).isPresent()) {
            throw new GameAlreadyInLibraryException();
        }
        UserGameLibrary libraryEntry = new UserGameLibrary(currentUser, game, request.status());
        UserGameLibrary savedLibraryEntry = userGameLibraryRepository.save(libraryEntry);

        UserGameProgress progress = userGameProgressRepository
                .findByUserIdAndGameId(currentUser.getId(), game.getId())
                .orElse(null);

        return toResponse(savedLibraryEntry, progress);

    }

    @Transactional
    public GameInLibraryResponse updateCurrentUserGameStatus(Long gameId, UpdateGameInLibraryStatusRequest request) {
        User currentUser = currentUserResolver.getCurrentUser();

        UserGameLibrary libraryEntry = userGameLibraryRepository.findByUserIdAndGameId(currentUser.getId(), gameId).orElseThrow(() -> new ResourceNotFoundException(
                "El juego no está en tu biblioteca"
        ));

        libraryEntry.updateStatus(request.status());
        UserGameLibrary savedLibraryEntry = userGameLibraryRepository.save(libraryEntry);

        UserGameProgress progress = userGameProgressRepository
                .findByUserIdAndGameId(currentUser.getId(), gameId)
                .orElse(null);

        return toResponse(savedLibraryEntry, progress);
    }

    @Transactional
    public GameInLibraryResponse updateCurrentUserGameFavorite(Long gameId, UpdateGameInLibraryFavoriteRequest request) {
        User currentUser = currentUserResolver.getCurrentUser();

        UserGameLibrary libraryEntry = userGameLibraryRepository.findByUserIdAndGameId(currentUser.getId(), gameId).orElseThrow(() -> new ResourceNotFoundException(
                "El juego no esta en tu biblioteca"
        ));
        libraryEntry.updateFavorite(request.favorite());

        UserGameLibrary savedLibraryEntry = userGameLibraryRepository.save(libraryEntry);

        UserGameProgress progress = userGameProgressRepository
                .findByUserIdAndGameId(currentUser.getId(), gameId)
                .orElse(null);

        return toResponse(savedLibraryEntry, progress);

    }

    @Transactional
    public void removeCurrentUserGame(Long gameId) {
        User currentUser = currentUserResolver.getCurrentUser();

        UserGameLibrary libraryEntry = userGameLibraryRepository.findByUserIdAndGameId(currentUser.getId(), gameId).orElseThrow(() -> new ResourceNotFoundException(
                "El juego no esta en la biblioteca"
        ));

        userGameLibraryRepository.delete(libraryEntry);
    }

    @Transactional
    public void ensureGameIsInLibraryAsPlaying(User user, Game game){
        if (userGameLibraryRepository.findByUserIdAndGameId(user.getId(), game.getId()).isEmpty()) {
            UserGameLibrary libraryEntry = new UserGameLibrary(user, game, UserGameLibraryStatus.PLAYING);
            userGameLibraryRepository.save(libraryEntry);
        }
    }

    private GameInLibraryResponse toResponse(UserGameLibrary libraryEntry, UserGameProgress progress) {
        String checkpointLabel = null;
        Integer checkpointPosition = null;
        if (progress != null) {
            checkpointLabel = progress.getCheckpoint().getLabel();
            checkpointPosition = progress.getCheckpoint().getPosition();
        }

        return new GameInLibraryResponse(
                libraryEntry.getGame().getId(),
                libraryEntry.getGame().getTitle(),
                libraryEntry.getGame().getCoverImageUrl(),
                checkpointLabel,
                checkpointPosition,
                libraryEntry.getStatus(),
                libraryEntry.isFavorite(),
                libraryEntry.getUpdatedAt()
        );
    }
}
