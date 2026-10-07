package com.umbral.domain.service;

import com.umbral.domain.dto.CreateJournalEntryRequest;
import com.umbral.domain.dto.JournalEntryResponse;
import com.umbral.domain.dto.JournalEntryFeedResponse;
import com.umbral.domain.dto.UpdateJournalEntryRequest;
import com.umbral.domain.entity.JournalEntryType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.umbral.domain.entity.Checkpoint;
import com.umbral.domain.entity.JournalEntry;
import com.umbral.domain.entity.User;
import com.umbral.domain.entity.UserGameProgress;
import com.umbral.domain.exception.AuthorCannotPublishBeyondProgressException;
import com.umbral.domain.exception.ResourceNotFoundException;
import com.umbral.domain.repository.CheckpointRepository;
import com.umbral.domain.repository.GameRepository;
import com.umbral.domain.repository.JournalEntryRepository;
import com.umbral.domain.repository.UserGameProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final CheckpointRepository checkpointRepository;
    private final GameRepository gameRepository;
    private final UserGameProgressRepository userGameProgressRepository;
    private final CurrentUserResolver currentUserResolver;

    public JournalEntryService(JournalEntryRepository journalEntryRepository, CheckpointRepository checkpointRepository, GameRepository gameRepository, UserGameProgressRepository userGameProgressRepository, CurrentUserResolver currentUserResolver) {
        this.journalEntryRepository = journalEntryRepository;
        this.checkpointRepository = checkpointRepository;
        this.gameRepository = gameRepository;
        this.userGameProgressRepository = userGameProgressRepository;
        this.currentUserResolver = currentUserResolver;
    }

    @Transactional
    public JournalEntryResponse createCurrentUserEntry(CreateJournalEntryRequest request) {
        User author = currentUserResolver.getCurrentUser();
        Checkpoint checkpoint = checkpointRepository.findById(request.checkpointId())
                .orElseThrow(() -> new ResourceNotFoundException("El checkpoint no fue encontrado."));


        UserGameProgress authorProgress = userGameProgressRepository
                .findByUserIdAndGameId(author.getId(), checkpoint.getGame().getId())
                .orElseThrow(() -> new AuthorCannotPublishBeyondProgressException("No podés publicar sobre un juego sin progreso"));

        if (authorProgress.getCheckpoint().getPosition() < checkpoint.getPosition()) {
            throw new AuthorCannotPublishBeyondProgressException("No podés publicar sobre un checkpoint al que todavía no llegaste.");
        }

        JournalEntry entry = new JournalEntry(
                author,
                checkpoint,
                request.type(),
                request.content()
        );

        JournalEntry savedEntry = journalEntryRepository.save(entry);

        return toResponse(savedEntry);
    }

    @Transactional
    public JournalEntryResponse updateCurrentUserEntry(Long entryId, UpdateJournalEntryRequest request) {
        User author = currentUserResolver.getCurrentUser();
        JournalEntry entry = journalEntryRepository.findByIdAndAuthorId(entryId, author.getId())
                .orElseThrow(() -> new ResourceNotFoundException("La publicación no está disponible."));
        UserGameProgress progress = userGameProgressRepository
                .findByUserIdAndGameId(author.getId(), entry.getCheckpoint().getGame().getId())
                .orElseThrow(() -> new ResourceNotFoundException("La publicación no está disponible."));
        if (progress.getCheckpoint().getPosition() < entry.getCheckpoint().getPosition()) {
            throw new ResourceNotFoundException("La publicación no está disponible.");
        }
        entry.update(request.type(), request.content());
        return toResponse(entry);
    }

    @Transactional(readOnly = true)
    public JournalEntryFeedResponse getVisibleEntriesForCurrentUser(
            Long gameId, JournalEntryType type, int page, int size, Sort.Direction order
    ) {
        User reader = currentUserResolver.getCurrentUser();

        gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("El juego no fue encontrado"));

        var entries = journalEntryRepository.findVisibleByReaderIdAndGameId(
                reader.getId(),
                gameId,
                type,
                PageRequest.of(page, size, Sort.by(order, "createdAt", "id"))
        );
        return new JournalEntryFeedResponse(
                entries.map(this::toResponse).getContent(), page, size, entries.hasNext()
        );
    }

    private JournalEntryResponse toResponse(JournalEntry entry) {
        return new JournalEntryResponse(
                entry.getId(),
                entry.getAuthor().getHandle(),
                entry.getCheckpoint().getGame().getId(),
                entry.getCheckpoint().getLabel(),
                entry.getType(),
                entry.getContent(),
                entry.getCreatedAt(),
                entry.getEditedAt()
        );
    }
}
