package com.umbral.web.controller;

import com.umbral.domain.dto.CreateJournalEntryRequest;
import com.umbral.domain.dto.JournalEntryResponse;
import com.umbral.domain.service.JournalEntryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    public JournalEntryController(JournalEntryService journalEntryService) {
        this.journalEntryService = journalEntryService;
    }

    @PostMapping("/me/journal-entries")
    public ResponseEntity<JournalEntryResponse> createJournalEntry(
            @Valid @RequestBody CreateJournalEntryRequest request
    ) {
        JournalEntryResponse entry = journalEntryService
                .createCurrentUserEntry(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }

    @GetMapping("/games/{gameId}/journal-entries")
    public ResponseEntity<List<JournalEntryResponse>> getJournalEntries(
            @PathVariable Long gameId
    ) {
        List<JournalEntryResponse> entries = journalEntryService
                .getVisibleEntriesForCurrentUser(gameId);

        return ResponseEntity.ok(entries);
    }
}