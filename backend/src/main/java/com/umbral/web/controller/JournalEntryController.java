package com.umbral.web.controller;

import com.umbral.domain.dto.CreateJournalEntryRequest;
import com.umbral.domain.dto.JournalEntryResponse;
import com.umbral.domain.dto.JournalEntryFeedResponse;
import com.umbral.domain.dto.UpdateJournalEntryRequest;
import com.umbral.domain.entity.JournalEntryType;
import org.springframework.data.domain.Sort;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import com.umbral.domain.service.JournalEntryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/me/journal-entries/{entryId}")
    public ResponseEntity<JournalEntryResponse> updateJournalEntry(
            @PathVariable Long entryId,
            @Valid @RequestBody UpdateJournalEntryRequest request
    ) {
        return ResponseEntity.ok(journalEntryService.updateCurrentUserEntry(entryId, request));
    }

    @GetMapping("/games/{gameId}/journal-entries")
    public ResponseEntity<JournalEntryFeedResponse> getJournalEntries(
            @PathVariable Long gameId,
            @RequestParam(required = false) JournalEntryType type,
            @RequestParam(defaultValue = "0") @Min(0) @Max(10000) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
            @RequestParam(defaultValue = "DESC") Sort.Direction order
    ) {
        JournalEntryFeedResponse entries = journalEntryService
                .getVisibleEntriesForCurrentUser(gameId, type, page, size, order);

        return ResponseEntity.ok(entries);
    }
}
