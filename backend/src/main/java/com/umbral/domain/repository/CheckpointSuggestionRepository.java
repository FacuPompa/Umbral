package com.umbral.domain.repository;

import com.umbral.domain.entity.CheckpointSuggestion;
import com.umbral.domain.entity.CheckpointSuggestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CheckpointSuggestionRepository extends JpaRepository<CheckpointSuggestion, Long> {
    boolean existsByGameIdAndPositionAndStatus(
            Long gameId,
            int position,
            CheckpointSuggestionStatus status
    );

    List<CheckpointSuggestion> findAllByStatusOrderByCreatedAtAscIdAsc(
            CheckpointSuggestionStatus status
    );
}
