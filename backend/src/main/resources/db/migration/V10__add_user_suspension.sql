ALTER TABLE app_users
    ADD COLUMN suspended BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN authentication_version BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN row_version BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN suspension_reason VARCHAR(500),
    ADD COLUMN account_status_changed_at TIMESTAMPTZ,
    ADD COLUMN account_status_changed_by_user_id BIGINT REFERENCES app_users(id),
    ADD CONSTRAINT ck_user_authentication_version CHECK (authentication_version >= 0),
    ADD CONSTRAINT ck_user_suspension_reason CHECK (
        (suspended = FALSE AND suspension_reason IS NULL)
        OR (suspended = TRUE AND suspension_reason IS NOT NULL
            AND length(trim(suspension_reason)) BETWEEN 3 AND 500)
    );
