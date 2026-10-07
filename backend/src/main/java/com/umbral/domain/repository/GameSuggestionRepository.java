package com.umbral.domain.repository;

import com.umbral.domain.entity.GameSuggestion;
import com.umbral.domain.entity.GameSuggestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface GameSuggestionRepository extends JpaRepository<GameSuggestion, Long> {
    @Query("""
            select suggestion from GameSuggestion suggestion
            where suggestion.suggestedBy.id = :userId
              and (:status is null or suggestion.status = :status)
            order by suggestion.createdAt desc, suggestion.id desc
            """)
    Slice<GameSuggestion> findOwnSuggestions(Long userId, GameSuggestionStatus status, Pageable pageable);

    boolean existsByRawgGameId(Long rawgGameId);

    List<GameSuggestion> findAllByStatusOrderByCreatedAtAscIdAsc(GameSuggestionStatus status);
}
