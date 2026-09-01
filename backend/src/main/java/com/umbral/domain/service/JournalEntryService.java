package com.umbral.domain.service;

import com.umbral.domain.dto.CreateJournalEntryRequest;
import com.umbral.domain.dto.JournalEntryResponse;
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

import java.util.List;

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

    @Transactional(readOnly = true)
    public List<JournalEntryResponse> getVisibleEntriesForCurrentUser(Long gameId) {
        User reader = currentUserResolver.getCurrentUser();

        gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("El juego no fue encontrado"));

        return journalEntryRepository.findVisibleByReaderIdAndGameId(
                reader.getId(),
                gameId
        )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private JournalEntryResponse toResponse(JournalEntry entry) {
        return new JournalEntryResponse(
                entry.getId(),
                entry.getAuthor().getHandle(),
                entry.getCheckpoint().getGame().getId(),
                entry.getCheckpoint().getLabel(),
                entry.getType(),
                entry.getContent(),
                entry.getCreatedAt()
        );
    }
}