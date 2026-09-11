package com.umbral.domain.service;

import com.umbral.domain.dto.CreateGameInLibraryRequest;
import com.umbral.domain.dto.GameInLibraryResponse;
import com.umbral.domain.dto.UpdateGameProgressRequest;
import com.umbral.domain.dto.UpdateGameInLibraryFavoriteRequest;
import com.umbral.domain.dto.UpdateGameInLibraryStatusRequest;
import com.umbral.domain.entity.Game;
import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserGameLibrary;
import com.umbral.domain.entity.UserGameLibraryStatus;
import com.umbral.domain.exception.GameAlreadyInLibraryException;
import com.umbral.domain.exception.ResourceNotFoundException;
import com.umbral.domain.repository.GameRepository;
import com.umbral.domain.repository.CheckpointRepository;
import com.umbral.domain.repository.UserGameLibraryRepository;
import com.umbral.domain.repository.UserGameProgressRepository;
import com.umbral.domain.repository.UserRepository;
import com.umbral.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@Transactional
class UserGameLibraryServiceTest {

    @Autowired
    private UserGameLibraryService userGameLibraryService;

    @Autowired
    private UserGameLibraryRepository userGameLibraryRepository;

    @Autowired
    private UserGameProgressRepository userGameProgressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CheckpointRepository checkpointRepository;

    @Autowired
    private GameProgressService gameProgressService;

    @Test
    @WithMockUser(username = "umbral-demo")
    void addsAGameToTheCurrentUsersLibrary() {
        Game persona5Royal = findPersona5Royal();

        GameInLibraryResponse response = userGameLibraryService.addCurrentUserGame(
                new CreateGameInLibraryRequest(
                        persona5Royal.getId(),
                        UserGameLibraryStatus.WANT_TO_PLAY
                )
        );

        User demoUser = userRepository.findByHandle("umbral-demo")
                .orElseThrow();
        UserGameLibrary savedEntry = userGameLibraryRepository
                .findByUserIdAndGameId(demoUser.getId(), persona5Royal.getId())
                .orElseThrow();

        assertEquals(persona5Royal.getId(), response.gameId());
        assertEquals("Persona 5 Royal", response.gameTitle());
        assertEquals(UserGameLibraryStatus.WANT_TO_PLAY, response.status());
        assertFalse(response.favorite());
        assertNull(response.checkpointLabel());
        assertNull(response.checkpointPosition());
        assertEquals(UserGameLibraryStatus.WANT_TO_PLAY, savedEntry.getStatus());
        assertFalse(savedEntry.isFavorite());
    }

    @Test
    @WithMockUser(username = "umbral-demo")
    void rejectsAddingTheSameGameTwice() {
        Game persona5Royal = findPersona5Royal();
        CreateGameInLibraryRequest request = new CreateGameInLibraryRequest(
                persona5Royal.getId(),
                UserGameLibraryStatus.PLAYING
        );

        userGameLibraryService.addCurrentUserGame(request);

        assertThrows(
                GameAlreadyInLibraryException.class,
                () -> userGameLibraryService.addCurrentUserGame(request)
        );
    }

    @Test
    @WithMockUser(username = "umbral-demo")
    void rejectsAddingAnUnknownGame() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> userGameLibraryService.addCurrentUserGame(
                        new CreateGameInLibraryRequest(
                                999_999L,
                                UserGameLibraryStatus.WANT_TO_PLAY
                        )
                )
        );
    }

    @Test
    @WithMockUser(username = "umbral-author-demo")
    void listsTheCheckpointOfAGameThatHasProgress() {
        List<GameInLibraryResponse> library = userGameLibraryService.getCurrentUserLibrary();

        assertEquals(1, library.size());
        GameInLibraryResponse response = library.getFirst();

        assertEquals("Persona 5 Royal", response.gameTitle());
        assertEquals(UserGameLibraryStatus.PLAYING, response.status());
        assertFalse(response.favorite());
        assertEquals("Palacio de Shido", response.checkpointLabel());
        assertEquals(8, response.checkpointPosition());
    }

    @Test
    @WithMockUser(username = "umbral-demo")
    void listsNullCheckpointWhenALibraryGameHasNoProgress() {
        Game persona5Royal = findPersona5Royal();
        userGameLibraryService.addCurrentUserGame(
                new CreateGameInLibraryRequest(
                        persona5Royal.getId(),
                        UserGameLibraryStatus.WANT_TO_PLAY
                )
        );

        List<GameInLibraryResponse> library = userGameLibraryService.getCurrentUserLibrary();

        assertEquals(1, library.size());
        assertNull(library.getFirst().checkpointLabel());
        assertNull(library.getFirst().checkpointPosition());
        assertTrue(library.getFirst().updatedAt() != null);
    }

    @Test
    @WithMockUser(username = "umbral-demo")
    void updatesTheStatusOfAGameInTheCurrentUsersLibrary() {
        Game persona5Royal = findPersona5Royal();
        userGameLibraryService.addCurrentUserGame(
                new CreateGameInLibraryRequest(persona5Royal.getId(), UserGameLibraryStatus.WANT_TO_PLAY)
        );

        GameInLibraryResponse response = userGameLibraryService.updateCurrentUserGameStatus(
                persona5Royal.getId(),
                new UpdateGameInLibraryStatusRequest(UserGameLibraryStatus.COMPLETED)
        );

        assertEquals(UserGameLibraryStatus.COMPLETED, response.status());
        assertEquals(
                UserGameLibraryStatus.COMPLETED,
                userGameLibraryRepository.findByUserIdAndGameId(
                        userRepository.findByHandle("umbral-demo").orElseThrow().getId(),
                        persona5Royal.getId()
                ).orElseThrow().getStatus()
        );
    }

    @Test
    @WithMockUser(username = "umbral-demo")
    void updatesWhetherAGameIsFavoriteInTheCurrentUsersLibrary() {
        Game persona5Royal = findPersona5Royal();
        userGameLibraryService.addCurrentUserGame(
                new CreateGameInLibraryRequest(persona5Royal.getId(), UserGameLibraryStatus.PLAYING)
        );

        GameInLibraryResponse response = userGameLibraryService.updateCurrentUserGameFavorite(
                persona5Royal.getId(),
                new UpdateGameInLibraryFavoriteRequest(true)
        );

        assertTrue(response.favorite());
        assertTrue(
                userGameLibraryRepository.findByUserIdAndGameId(
                        userRepository.findByHandle("umbral-demo").orElseThrow().getId(),
                        persona5Royal.getId()
                ).orElseThrow().isFavorite()
        );
    }

    @Test
    @WithMockUser(username = "umbral-demo")
    void removesAGameFromTheCurrentUsersLibraryWithoutDeletingProgress() {
        Game persona5Royal = findPersona5Royal();
        gameProgressService.updateCurrentUserProgress(
                persona5Royal.getId(),
                new UpdateGameProgressRequest(
                        checkpointRepository.findByGameIdOrderByPositionAsc(persona5Royal.getId()).getFirst().getId()
                )
        );

        userGameLibraryService.removeCurrentUserGame(persona5Royal.getId());

        User demoUser = userRepository.findByHandle("umbral-demo").orElseThrow();
        assertTrue(userGameLibraryRepository.findByUserIdAndGameId(demoUser.getId(), persona5Royal.getId()).isEmpty());
        assertTrue(userGameProgressRepository.findByUserIdAndGameId(demoUser.getId(), persona5Royal.getId()).isPresent());
    }

    private Game findPersona5Royal() {
        return gameRepository.findAll().stream()
                .filter(game -> game.getTitle().equals("Persona 5 Royal"))
                .findFirst()
                .orElseThrow();
    }
}
