package com.umbral.web.controller;

import com.umbral.domain.dto.CreateJournalReplyRequest;
import com.umbral.domain.dto.JournalReplyResponse;
import com.umbral.domain.service.JournalReplyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class JournalReplyController {

    private final JournalReplyService journalReplyService;

    public JournalReplyController(JournalReplyService journalReplyService) {
        this.journalReplyService = journalReplyService;
    }

    @GetMapping("/journal-entries/{entryId}/replies")
    public ResponseEntity<List<JournalReplyResponse>> getReplies(@PathVariable Long entryId) {
        List<JournalReplyResponse> replies =
                journalReplyService.getVisibleRepliesForCurrentUser(entryId);

        return ResponseEntity.ok(replies);
    }

    @PostMapping("/me/journal-entries/{entryId}/replies")
    public ResponseEntity<JournalReplyResponse> createReply(@PathVariable Long entryId, @Valid @RequestBody CreateJournalReplyRequest request) {
        JournalReplyResponse reply =
                journalReplyService.createCurrentUserReply(entryId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(reply);
    }
}