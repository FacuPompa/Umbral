-- Legacy records retain unknown authors and dates. Do not backfill identities.
ALTER TABLE games
    ADD COLUMN created_by_user_id BIGINT REFERENCES app_users(id),
    ADD COLUMN created_at TIMESTAMPTZ,
    ADD COLUMN updated_by_user_id BIGINT REFERENCES app_users(id),
    ADD COLUMN updated_at TIMESTAMPTZ;

ALTER TABLE checkpoints
    ADD COLUMN created_by_user_id BIGINT REFERENCES app_users(id),
    ADD COLUMN created_at TIMESTAMPTZ,
    ADD COLUMN updated_by_user_id BIGINT REFERENCES app_users(id),
    ADD COLUMN updated_at TIMESTAMPTZ;
