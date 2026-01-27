CREATE TABLE users
(
    id               UUID        NOT NULL,
    supabase_user_id UUID,
    email            VARCHAR(255) NOT NULL,
    guest            BOOLEAN     NOT NULL DEFAULT FALSE,
    guest_id         VARCHAR(255),
    created_at       TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE TABLE subscriptions
(
    id                     UUID         NOT NULL,
    user_id                UUID         NOT NULL,
    stripe_customer_id     VARCHAR(255),
    stripe_subscription_id VARCHAR(255),
    plan_code              VARCHAR(255) NOT NULL,
    status                 VARCHAR(255) NOT NULL,
    current_period_end     TIMESTAMPTZ,
    cancel_at_period_end   BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_subscriptions PRIMARY KEY (id)
);

CREATE TABLE drop_session
(
    id         UUID         NOT NULL,
    owner_id   UUID         NOT NULL,
    code       VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    ended_at   TIMESTAMPTZ,
    status     VARCHAR(255) NOT NULL,
    end_reason VARCHAR(255),
    CONSTRAINT pk_drop_session PRIMARY KEY (id)
);

CREATE TABLE drop_session_participants
(
    id              UUID         NOT NULL,
    drop_session_id UUID         NOT NULL,
    user_id         UUID,
    role            VARCHAR(255) NOT NULL,
    joined_at       TIMESTAMPTZ  NOT NULL,
    disconnected_at TIMESTAMPTZ,
    auto_download   BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_drop_session_participants PRIMARY KEY (id)
);

CREATE TABLE drop_session_note
(
    id             UUID         NOT NULL,
    session_id     UUID         NOT NULL,
    participant_id UUID         NOT NULL,
    content        VARCHAR(500) NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL,
    CONSTRAINT pk_drop_session_note PRIMARY KEY (id)
);

CREATE TABLE drop_file
(
    id              UUID         NOT NULL,
    drop_session_id UUID         NOT NULL,
    uploader        UUID         NOT NULL,
    storage_path    VARCHAR(1024) NOT NULL,
    original_name   VARCHAR(1024),
    size_bytes      BIGINT,
    created_at      TIMESTAMPTZ  NOT NULL,
    CONSTRAINT pk_dropfile PRIMARY KEY (id)
);

CREATE TABLE drop_file_download
(
    id             UUID        NOT NULL,
    file_id        UUID        NOT NULL,
    participant_id UUID        NOT NULL,
    downloaded_at  TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_drop_file_download PRIMARY KEY (id)
);

CREATE TABLE stripe_webhook_events
(
    id              UUID         NOT NULL,
    stripe_event_id VARCHAR(255) NOT NULL,
    type            VARCHAR(255),
    livemode        BOOLEAN,
    received_at     TIMESTAMPTZ  NOT NULL,
    processed_at    TIMESTAMPTZ,
    status          VARCHAR(50)  NOT NULL,
    attempt_count   INTEGER      NOT NULL,
    next_retry_at   TIMESTAMPTZ,
    last_error      TEXT,
    payload         TEXT         NOT NULL,
    sig_header      TEXT         NOT NULL,
    CONSTRAINT pk_stripe_webhook_events PRIMARY KEY (id)
);

-- ---------- UNIQUE CONSTRAINTS ----------
ALTER TABLE drop_session_participants
    ADD CONSTRAINT uc_77d3792cdc3063d15dc921307 UNIQUE (drop_session_id, user_id);

ALTER TABLE drop_session
    ADD CONSTRAINT uc_drop_session_code UNIQUE (code);

ALTER TABLE subscriptions
    ADD CONSTRAINT uc_subscriptions_stripe_customer UNIQUE (stripe_customer_id);

ALTER TABLE subscriptions
    ADD CONSTRAINT uc_subscriptions_stripe_subscription UNIQUE (stripe_subscription_id);

ALTER TABLE subscriptions
    ADD CONSTRAINT uc_subscriptions_user UNIQUE (user_id);

ALTER TABLE users
    ADD CONSTRAINT uc_users_guest UNIQUE (guest_id);

ALTER TABLE users
    ADD CONSTRAINT uc_users_supabase_user UNIQUE (supabase_user_id);

ALTER TABLE drop_file_download
    ADD CONSTRAINT uq_download_participant_file UNIQUE (participant_id, file_id);

ALTER TABLE stripe_webhook_events
    ADD CONSTRAINT ux_stripe_webhook_events_event_id UNIQUE (stripe_event_id);

-- ---------- INDEXES ----------
CREATE INDEX idx_drop_file_session_created ON drop_file (drop_session_id, created_at);

CREATE INDEX idx_drop_session_code ON drop_session (code);
CREATE INDEX idx_drop_session_ended_at ON drop_session (ended_at);
CREATE INDEX idx_drop_session_owner_status_expires ON drop_session (owner_id, status, expires_at);
CREATE INDEX idx_drop_session_status_expires ON drop_session (status, expires_at);

CREATE INDEX idx_participant_user_session ON drop_session_participants (user_id, drop_session_id);
CREATE INDEX idx_participant_session ON drop_session_participants (drop_session_id);
CREATE INDEX idx_participant_user ON drop_session_participants (user_id);

CREATE INDEX idx_download_file ON drop_file_download (file_id);
CREATE INDEX idx_download_participant ON drop_file_download (participant_id);

-- ---------- FOREIGN KEYS (with ON DELETE behaviors) ----------
ALTER TABLE drop_file
    ADD CONSTRAINT FK_DROPFILE_ON_DROP_SESSION
        FOREIGN KEY (drop_session_id) REFERENCES drop_session (id) ON DELETE CASCADE;

ALTER TABLE drop_file
    ADD CONSTRAINT FK_DROPFILE_ON_UPLOADER
        FOREIGN KEY (uploader) REFERENCES users (id);

ALTER TABLE drop_file_download
    ADD CONSTRAINT FK_DROP_FILE_DOWNLOAD_ON_FILE
        FOREIGN KEY (file_id) REFERENCES drop_file (id) ON DELETE CASCADE;

ALTER TABLE drop_file_download
    ADD CONSTRAINT FK_DROP_FILE_DOWNLOAD_ON_PARTICIPANT
        FOREIGN KEY (participant_id) REFERENCES drop_session_participants (id) ON DELETE CASCADE;

ALTER TABLE drop_session_note
    ADD CONSTRAINT FK_DROP_SESSION_NOTE_ON_PARTICIPANT
        FOREIGN KEY (participant_id) REFERENCES drop_session_participants (id) ON DELETE CASCADE;

ALTER TABLE drop_session_note
    ADD CONSTRAINT FK_DROP_SESSION_NOTE_ON_SESSION
        FOREIGN KEY (session_id) REFERENCES drop_session (id) ON DELETE CASCADE;

ALTER TABLE drop_session
    ADD CONSTRAINT FK_DROP_SESSION_ON_OWNER
        FOREIGN KEY (owner_id) REFERENCES users (id);

ALTER TABLE drop_session_participants
    ADD CONSTRAINT FK_DROP_SESSION_PARTICIPANTS_ON_DROP_SESSION
        FOREIGN KEY (drop_session_id) REFERENCES drop_session (id) ON DELETE CASCADE;

ALTER TABLE drop_session_participants
    ADD CONSTRAINT FK_DROP_SESSION_PARTICIPANTS_ON_USER
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE subscriptions
    ADD CONSTRAINT FK_SUBSCRIPTIONS_ON_USER
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
