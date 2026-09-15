package com.umbral.domain.repository;


import com.umbral.domain.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByRawgGameId(Long rawgGameId);

    boolean existsByRawgGameId(Long rawgGameId);

    boolean existsByTitle(String title);

    List<Game> findTop5ByTitleContainingIgnoreCaseOrderByTitleAsc(String query);
}
