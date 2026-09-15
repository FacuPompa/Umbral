package com.umbral.domain.service;

import com.umbral.domain.dto.PublicUserProfileResponse;
import com.umbral.domain.entity.Game;
import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserGameLibrary;
import com.umbral.domain.entity.UserGameLibraryStatus;
import com.umbral.domain.entity.UserRole;
import com.umbral.domain.exception.ResourceNotFoundException;
import com.umbral.domain.repository.GameRepository;
import com.umbral.domain.repository.UserGameLibraryRepository;
import com.umbral.domain.repository.UserRepository;
import com.umbral.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@Transactional
class PublicProfileServiceTest {

    @Autowired
    private PublicProfileService publicProfileService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGameLibraryRepository userGameLibraryRepository;

    @Autowired
    private GameRepository gameRepository;

    @Test
    void returnsOnlyThePublicSummaryAndFavoriteGames() {
        User user = userRepository.save(new User(
                "public-profile-test",
                "public-profile-test@umbral.local",
                "not-a-real-password-hash",
                UserRole.MEMBER
        ));
        Game persona5Royal = findPersona5Royal();
        UserGameLibrary favorite = new UserGameLibrary(user, persona5Royal, UserGameLibraryStatus.COMPLETED);
        favorite.updateFavorite(true);
        userGameLibraryRepository.save(favorite);

        PublicUserProfileResponse response = publicProfileService.getProfileByHandle(user.getHandle());

        assertEquals("public-profile-test", response.handle());
        assertEquals(1, response.libraryCount());
        assertEquals(1, response.completedCount());
        assertEquals(1, response.favoriteCount());
        assertEquals(1, response.favoriteGames().size());
        assertEquals(persona5Royal.getId(), response.favoriteGames().getFirst().gameId());
        assertEquals("Persona 5 Royal", response.favoriteGames().getFirst().gameTitle());
    }

    @Test
    void rejectsAnUnknownHandle() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> publicProfileService.getProfileByHandle("missing-profile")
        );
    }

    private Game findPersona5Royal() {
        return gameRepository.findAll().stream()
                .filter(game -> game.getTitle().equals("Persona 5 Royal"))
                .findFirst()
                .orElseThrow();
    }
}
