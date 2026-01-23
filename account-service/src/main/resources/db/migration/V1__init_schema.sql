CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');

CREATE TABLE users
(
    user_id  BIGINT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email    VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(128)        NOT NULL,
    role     user_role           NOT NULL DEFAULT 'USER',

    CONSTRAINT chk_email_correct CHECK (
        email ~ '^[^@]+@[^@]+\.[^@]+$'
        ),

    CONSTRAINT chk_password_long CHECK (
        LENGTH(password) >= 6
        ),

    CONSTRAINT chk_username_format CHECK (
        username NOT LIKE '%@%'
        )
);