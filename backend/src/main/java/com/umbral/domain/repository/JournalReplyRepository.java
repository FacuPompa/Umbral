package com.umbral.domain.repository;

import com.umbral.domain.entity.JournalReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JournalReplyRepository extends JpaRepository<JournalReply, Long> {
    List<JournalReply> findByJournalEntryIdOrderByCreatedAtAscIdAsc(Long journalEntryId);
}
