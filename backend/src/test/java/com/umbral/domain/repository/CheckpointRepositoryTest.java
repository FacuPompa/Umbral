package com.umbral.domain.repository;

import com.umbral.domain.entity.Checkpoint;
import com.umbral.domain.entity.Game;
import com.umbral.support.PostgresTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
class CheckpointRepositoryTest {

    @Autowired
    private CheckpointRepository checkpointRepository;

    @Autowired
    private GameRepository gameRepository;

    @Test
    void findsCheckpointsOfAGameOrderedByPosition() {
        Game persona5Royal = gameRepository.findAll().stream()
                .filter(game -> game.getTitle().equals("Persona 5 Royal"))
                .findFirst()
                .orElseThrow();
        List<Checkpoint> checkpoints =
                checkpointRepository.findByGameIdOrderByPositionAsc(persona5Royal.getId());

        assertEquals(10, checkpoints.size());

        for (int index = 0; index < checkpoints.size(); index++) {
            assertEquals(index + 1, checkpoints.get(index).getPosition());
        }
    }
}