package com.umbral.domain.repository;

import com.umbral.domain.entity.GameSuggestion;
import com.umbral.domain.entity.GameSuggestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameSuggestionRepository extends JpaRepository<GameSuggestion, Long> {
    boolean existsByRawgGameId(Long rawgGameId);

    List<GameSuggestion> findAllByStatusOrderByCreatedAtAscIdAsc(GameSuggestionStatus status);
}
