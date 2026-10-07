package com.umbral.domain.repository;

import com.umbral.domain.entity.CheckpointSuggestion;
import com.umbral.domain.entity.CheckpointSuggestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface CheckpointSuggestionRepository extends JpaRepository<CheckpointSuggestion, Long> {
    @EntityGraph(attributePaths = {"game"})
    @Query("""
            select suggestion from CheckpointSuggestion suggestion
            where suggestion.suggestedBy.id = :userId
              and (:status is null or suggestion.status = :status)
            order by suggestion.createdAt desc, suggestion.id desc
            """)
    Slice<CheckpointSuggestion> findOwnSuggestions(Long userId, CheckpointSuggestionStatus status, Pageable pageable);

    boolean existsByGameIdAndPositionAndStatus(
            Long gameId,
            int position,
            CheckpointSuggestionStatus status
    );

    List<CheckpointSuggestion> findAllByStatusOrderByCreatedAtAscIdAsc(
            CheckpointSuggestionStatus status
    );
}
