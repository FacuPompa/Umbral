package com.umbral.domain.service;

import com.umbral.domain.dto.GameProgressResponse;
import com.umbral.domain.dto.UpdateGameProgressRequest;
import com.umbral.domain.entity.Checkpoint;
import com.umbral.domain.entity.Game;
import com.umbral.domain.repository.CheckpointRepository;
import com.umbral.domain.repository.GameRepository;
import com.umbral.domain.repository.UserGameProgressRepository;
import com.umbral.support.PostgresTestConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@Transactional
class GameProgressServiceTest {

    @Autowired
    private GameProgressService gameProgressService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CheckpointRepository checkpointRepository;

    @Autowired
    private UserGameProgressRepository userGameProgressRepository;

    @Test
    void createsProgressForCurrentUserWhenItDoesNotExist(){
        Game persona5Royal = gameRepository.findAll().stream()
                .filter(game -> game.getTitle().equals("Persona 5 Royal"))
                .findFirst()
                .orElseThrow();
        List<Checkpoint> checkpoints =
                checkpointRepository.findByGameIdOrderByPositionAsc((persona5Royal.getId()));

        assertEquals(10, checkpoints.size());

        Checkpoint kamoshida = checkpoints.get(1);

        UpdateGameProgressRequest request =
                new UpdateGameProgressRequest(kamoshida.getId());

        GameProgressResponse response =
                gameProgressService.updateCurrentUserProgress(persona5Royal.getId(), request);

        assertEquals(kamoshida.getId(), response.checkpointId());
        assertEquals(2, response.position());

        assertEquals(
                1,
                userGameProgressRepository.findAllByUserId(1L).size()
        );
        }
    @Test
    void updatesExistingProgressInsteadOfCreatingAnother() {
        Game persona5Royal = gameRepository.findAll().stream()
                .filter(game -> game.getTitle().equals("Persona 5 Royal"))
                .findFirst()
                .orElseThrow();
        List<Checkpoint> checkpoints =
                checkpointRepository.findByGameIdOrderByPositionAsc(persona5Royal.getId());

        Checkpoint kamoshida = checkpoints.get(1);
        Checkpoint madarame = checkpoints.get(2);

        gameProgressService.updateCurrentUserProgress(
                persona5Royal.getId(),
                new UpdateGameProgressRequest(kamoshida.getId())
        );

        GameProgressResponse response = gameProgressService.updateCurrentUserProgress(
                persona5Royal.getId(),
                new UpdateGameProgressRequest(madarame.getId())
        );

        assertEquals(madarame.getId(), response.checkpointId());
        assertEquals(3, response.position());
        assertEquals(1, userGameProgressRepository.findAllByUserId(1L).size());
        assertEquals(
                madarame.getId(),
                userGameProgressRepository.findAllByUserId(1L)
                        .getFirst()
                        .getCheckpoint()
                        .getId()
        );
    }

}
