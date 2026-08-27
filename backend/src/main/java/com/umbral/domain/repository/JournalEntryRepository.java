package com.umbral.domain.repository;

import com.umbral.domain.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    @Query("""
            SELECT entry
            FROM JournalEntry entry
            WHERE entry.checkpoint.game.id = :gameId
              AND entry.checkpoint.position <= (
                  SELECT progress.checkpoint.position
                  FROM UserGameProgress progress
                  WHERE progress.user.id = :userId
                    AND progress.game.id = :gameId
              )
            ORDER BY entry.createdAt DESC, entry.id DESC
            """)
    List<JournalEntry> findVisibleByReaderIdAndGameId(
            @Param("userId") Long userId,
            @Param("gameId") Long gameId
    );
}