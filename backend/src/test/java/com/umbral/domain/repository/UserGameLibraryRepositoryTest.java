package com.umbral.domain.repository;

import com.umbral.domain.entity.Game;
import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserGameLibrary;
import com.umbral.domain.entity.UserGameLibraryStatus;
import com.umbral.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@Transactional
class UserGameLibraryRepositoryTest {

    @Autowired
    private UserGameLibraryRepository userGameLibraryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Test
    void findsAnEntryByUserAndGameAndListsTheUsersLibrary() {
        User author = userRepository.findByHandle("umbral-author-demo")
                .orElseThrow();
        Game persona5Royal = gameRepository.findAll().stream()
                .filter(game -> game.getTitle().equals("Persona 5 Royal"))
                .findFirst()
                .orElseThrow();

        UserGameLibrary entry = userGameLibraryRepository
                .findByUserIdAndGameId(author.getId(), persona5Royal.getId())
                .orElseThrow();
        List<UserGameLibrary> library = userGameLibraryRepository
                .findAllByUserIdOrderByUpdatedAtDesc(author.getId());

        assertEquals(UserGameLibraryStatus.PLAYING, entry.getStatus());
        assertTrue(library.stream().anyMatch(item -> item.getId().equals(entry.getId())));
    }
}
