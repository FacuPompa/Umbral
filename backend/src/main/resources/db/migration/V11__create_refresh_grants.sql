CREATE TABLE refresh_grants (
    id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id),
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    authentication_version BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_refresh_grants_user ON refresh_grants(user_id);
CREATE TABLE used_refresh_tokens (
    token_hash VARCHAR(64) PRIMARY KEY,
    grant_id UUID NOT NULL REFERENCES refresh_grants(id) ON DELETE CASCADE
);
CREATE INDEX idx_used_refresh_tokens_grant ON used_refresh_tokens(grant_id);
