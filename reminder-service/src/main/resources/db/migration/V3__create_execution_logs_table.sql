CREATE TABLE execution_logs (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    reminder_id      UUID        NOT NULL REFERENCES reminders(id) ON DELETE CASCADE,
    idempotency_key  VARCHAR(255) UNIQUE NOT NULL,
    scheduled_time   TIMESTAMPTZ NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    executed_at      TIMESTAMPTZ,
    error_message    TEXT,
    retry_count      INT         NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_execution_logs_idempotency ON execution_logs(idempotency_key);
CREATE INDEX idx_execution_logs_reminder_id ON execution_logs(reminder_id);
CREATE INDEX idx_execution_logs_status ON execution_logs(status);