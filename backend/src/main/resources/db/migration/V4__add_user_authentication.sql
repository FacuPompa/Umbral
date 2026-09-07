ALTER TABLE app_users
    ADD COLUMN email VARCHAR(254),
    ADD COLUMN password_hash VARCHAR(255),
    ADD COLUMN role VARCHAR(20);

UPDATE app_users
SET
    email = handle || '@demo.umbral.local',
    password_hash = '$2y$12$1k4vwXDNqHvS13SHLPyuK.Hh/sXN7TacnhzDMvJ3SnzWbKCV/o8ya',
    role = 'MEMBER'
WHERE email IS NULL;

ALTER TABLE app_users
    ALTER COLUMN email SET NOT NULL,
    ALTER COLUMN password_hash SET NOT NULL,
    ALTER COLUMN role SET NOT NULL;

ALTER TABLE app_users
    ADD CONSTRAINT uq_app_users_email UNIQUE (email),
    ADD CONSTRAINT ck_app_users_role CHECK (role IN ('MEMBER', 'MODERATOR'));
