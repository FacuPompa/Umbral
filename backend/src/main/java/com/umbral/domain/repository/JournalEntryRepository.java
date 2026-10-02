package com.umbral.domain.repository;

import com.umbral.domain.entity.JournalEntry;
import com.umbral.domain.entity.JournalEntryType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    @EntityGraph(attributePaths = {"author", "checkpoint", "checkpoint.game"})
    @Query("""
            SELECT entry
            FROM JournalEntry entry
            WHERE entry.checkpoint.game.id = :gameId
              AND (:type IS NULL OR entry.type = :type)
              AND entry.checkpoint.position <= (
                  SELECT progress.checkpoint.position
                  FROM UserGameProgress progress
                  WHERE progress.user.id = :userId
                    AND progress.game.id = :gameId
              )
            """)
    Slice<JournalEntry> findVisibleByReaderIdAndGameId(
            @Param("userId") Long userId,
            @Param("gameId") Long gameId,
            @Param("type") JournalEntryType type,
            Pageable pageable
    );
}
