package com.umbral.domain.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "journal_entries")
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checkpoint_id", nullable = false)
    private Checkpoint checkpoint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JournalEntryType type;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "edited_at")
    private Instant editedAt;

    protected JournalEntry(){}

    public JournalEntry(User author, Checkpoint checkpoint, JournalEntryType type, String content) {
        this.author = author;
        this.checkpoint = checkpoint;
        this.type = type;
        this.content = content;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public User getAuthor() {
        return author;
    }

    public Checkpoint getCheckpoint() {
        return checkpoint;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public JournalEntryType getType() {
        return type;
    }

    public Instant getEditedAt() {
        return editedAt;
    }

    public void update(JournalEntryType type, String content) {
        if (this.type == type && this.content.equals(content)) return;
        this.type = type;
        this.content = content;
        this.editedAt = Instant.now();
    }
}
