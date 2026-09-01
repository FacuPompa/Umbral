ALTER TABLE games
    ALTER COLUMN title SET NOT NULL,
    ALTER COLUMN description SET NOT NULL;

ALTER TABLE journal_entries
    ADD COLUMN type VARCHAR(20);

UPDATE journal_entries
SET type = 'REFLECTION'
WHERE type IS NULL;

ALTER TABLE journal_entries
    ALTER COLUMN type SET NOT NULL;

ALTER TABLE journal_entries
    ADD CONSTRAINT ck_journal_entries_type
    CHECK (type IN ('REFLECTION', 'QUESTION', 'THEORY', 'REVIEW'));