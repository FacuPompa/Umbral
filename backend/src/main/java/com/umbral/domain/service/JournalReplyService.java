package com.umbral.domain.service;

import com.umbral.domain.dto.CreateJournalReplyRequest;
import com.umbral.domain.dto.JournalReplyResponse;
import com.umbral.domain.entity.JournalEntry;
import com.umbral.domain.entity.JournalReply;
import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserGameProgress;
import com.umbral.domain.exception.ResourceNotFoundException;
import com.umbral.domain.repository.JournalEntryRepository;
import com.umbral.domain.repository.JournalReplyRepository;
import com.umbral.domain.repository.UserGameProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JournalReplyService {

    private final JournalReplyRepository journalReplyRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final UserGameProgressRepository userGameProgressRepository;
    private final CurrentUserResolver currentUserResolver;

    public JournalReplyService(JournalReplyRepository journalReplyRepository, JournalEntryRepository journalEntryRepository, UserGameProgressRepository userGameProgressRepository, CurrentUserResolver currentUserResolver) {
        this.journalReplyRepository = journalReplyRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.userGameProgressRepository = userGameProgressRepository;
        this.currentUserResolver = currentUserResolver;
    }

    @Transactional(readOnly = true)
    public List<JournalReplyResponse> getVisibleRepliesForCurrentUser(Long entryId) {
        User currentUser = currentUserResolver.getCurrentUser();
        JournalEntry entry = getVisibleEntryForCurrentUser(currentUser, entryId);

        return journalReplyRepository
                .findByJournalEntryIdOrderByCreatedAtAscIdAsc(entry.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public JournalReplyResponse createCurrentUserReply(Long entryId, CreateJournalReplyRequest request) {
        User currentUser = currentUserResolver.getCurrentUser();
        JournalEntry entry = getVisibleEntryForCurrentUser(currentUser, entryId);

        JournalReply reply = new JournalReply(
                entry,
                currentUser,
                request.content()
        );

        JournalReply savedReply = journalReplyRepository.save(reply);

        return toResponse(savedReply);
    }

    private JournalEntry getVisibleEntryForCurrentUser(User currentUser, Long entryId) {
        JournalEntry entry = journalEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La entrada no fue encontrada."
                ));

        UserGameProgress progress = userGameProgressRepository
                .findByUserIdAndGameId(
                        currentUser.getId(),
                        entry.getCheckpoint().getGame().getId()
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La entrada no fue encontrada."
                ));

        if (progress.getCheckpoint().getPosition() < entry.getCheckpoint().getPosition()) {
            throw new ResourceNotFoundException("La entrada no fue encontrada.");
        }

        return entry;
    }

    private JournalReplyResponse toResponse(JournalReply reply) {
        return new JournalReplyResponse(
                reply.getId(),
                reply.getAuthor().getHandle(),
                reply.getContent(),
                reply.getCreatedAt()
        );
    }
}