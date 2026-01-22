CREATE TABLE users
(
    user_id    BIGINT PRIMARY KEY,
    username   VARCHAR(100) UNIQUE NOT NULL,
    email      VARCHAR(255) UNIQUE NOT NULL,
    password   VARCHAR(128)        NOT NULL,
    created_at timestamptz         NOT NULL DEFAULT NOW(),
    updated_at timestamptz,

    CONSTRAINT chk_email_correct CHECK (
        email ~ '^[^@]+@[^@]+\.[^@]+$'
        ),

    CONSTRAINT chk_password_long CHECK (
        LENGTH(password) >= 6
        ),

    CONSTRAINT chk_updated_at_in_future CHECK (
        updated_at IS NULL
            OR
        updated_at >= created_at
        )
);

CREATE INDEX idx_user_username ON users (username);
CREATE INDEX idx_user_email ON users (email);
CREATE INDEX idx_user_username_email ON users (username, email);