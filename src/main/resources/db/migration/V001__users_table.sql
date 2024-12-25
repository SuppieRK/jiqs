CREATE TABLE users
(
    -- Tip: check out https://uuid7.com
    id         UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    name       VARCHAR(128)             NOT NULL,
    password   VARCHAR(128)             NOT NULL
);