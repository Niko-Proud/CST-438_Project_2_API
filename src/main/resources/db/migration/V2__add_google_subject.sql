-- Stores Google's stable OpenID Connect subject identifier, not a bearer token.
-- Google identity documentation: https://openid.net/specs/openid-connect-core-1_0.html#SubjectID
-- Codex was used to help generate this incremental migration for the Google subject column.
-- The assistance focused on safe deployment to an existing database using a nullable, unique identity field.

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS google_sub VARCHAR(128);

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_google_sub
    ON users (google_sub)
    WHERE google_sub IS NOT NULL;
