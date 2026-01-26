CREATE TABLE users (
    user_id BIGINT PRIMARY KEY,
    username VARCHAR UNIQUE NOT NULL
);

CREATE TYPE task_status AS ENUM ('CREATED', 'CANCELLED', 'IN_BACKLOG', 'BLOCKED', 'DONE');

CREATE TABLE tasks (
    task_id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    owner_id BIGINT NOT NULL REFERENCES users(user_id),
    title TEXT NOT NULL CHECK ( length(title) > 0 ),
    content TEXT,
    status task_status NOT NULL DEFAULT 'CREATED',
    finished_at TIMESTAMPTZ,

    CONSTRAINT chk_finished_at_requires_done CHECK (finished_at IS NULL OR status = 'DONE')
);

CREATE INDEX idx_task_title ON tasks(owner_id, title);
CREATE INDEX idx_task_status ON tasks(owner_id, status);
CREATE INDEX idx_task_finished_at ON tasks(owner_id, finished_at);