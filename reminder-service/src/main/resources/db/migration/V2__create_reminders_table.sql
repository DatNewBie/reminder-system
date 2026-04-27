CREATE TABLE reminders (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title         VARCHAR(255) NOT NULL,
    message       TEXT,
    rrule         TEXT,
    start_time    TIMESTAMPTZ NOT NULL,
    next_run_time TIMESTAMPTZ NOT NULL,
    is_active     BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_reminders_user_id ON reminders(user_id);
CREATE INDEX idx_reminders_next_run_time ON reminders(next_run_time) WHERE is_active = TRUE;