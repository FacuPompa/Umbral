package com.umbral.domain.service;

import com.umbral.domain.dto.PublicProfileFavoriteGameResponse;
import com.umbral.domain.dto.PublicUserProfileResponse;
import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserGameLibrary;
import com.umbral.domain.entity.UserGameLibraryStatus;
import com.umbral.domain.exception.ResourceNotFoundException;
import com.umbral.domain.repository.UserGameLibraryRepository;
import com.umbral.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PublicProfileService {

    private final UserRepository userRepository;
    private final UserGameLibraryRepository userGameLibraryRepository;

    public PublicProfileService(
            UserRepository userRepository,
            UserGameLibraryRepository userGameLibraryRepository
    ) {
        this.userRepository = userRepository;
        this.userGameLibraryRepository = userGameLibraryRepository;
    }

    @Transactional(readOnly = true)
    public PublicUserProfileResponse getProfileByHandle(String handle) {
        User user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new ResourceNotFoundException("No existe ese perfil."));

        List<PublicProfileFavoriteGameResponse> favoriteGames = userGameLibraryRepository
                .findAllByUserIdAndFavoriteTrueOrderByUpdatedAtDesc(user.getId())
                .stream()
                .map(this::toFavoriteGameResponse)
                .toList();

        return new PublicUserProfileResponse(
                user.getHandle(),
                userGameLibraryRepository.countByUserId(user.getId()),
                userGameLibraryRepository.countByUserIdAndStatus(user.getId(), UserGameLibraryStatus.COMPLETED),
                favoriteGames.size(),
                favoriteGames
        );
    }

    private PublicProfileFavoriteGameResponse toFavoriteGameResponse(UserGameLibrary libraryEntry) {
        return new PublicProfileFavoriteGameResponse(
                libraryEntry.getGame().getId(),
                libraryEntry.getGame().getTitle(),
                libraryEntry.getGame().getCoverImageUrl()
        );
    }
}
