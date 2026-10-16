INSERT INTO roles (name) VALUES ('EMPLOYEE') ON CONFLICT (name) DO NOTHING;

ALTER TABLE users
    ADD COLUMN assigned_store_id BIGINT;

ALTER TABLE users
    ADD CONSTRAINT fk_users_assigned_store
        FOREIGN KEY (assigned_store_id) REFERENCES stores (id) ON DELETE SET NULL;

CREATE INDEX ix_users_assigned_store ON users (assigned_store_id);

CREATE TABLE support_conversations (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    employee_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMPTZ,
    CONSTRAINT fk_support_conversations_customer FOREIGN KEY (customer_id) REFERENCES users (id),
    CONSTRAINT fk_support_conversations_store FOREIGN KEY (store_id) REFERENCES stores (id),
    CONSTRAINT fk_support_conversations_employee FOREIGN KEY (employee_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_support_conversations_status CHECK (status IN ('OPEN', 'CLOSED'))
);

CREATE UNIQUE INDEX uk_support_conversations_open_customer_store
    ON support_conversations (customer_id, store_id) WHERE status = 'OPEN';
CREATE INDEX ix_support_conversations_employee_status
    ON support_conversations (employee_id, status, updated_at DESC);
CREATE INDEX ix_support_conversations_store_status
    ON support_conversations (store_id, status, updated_at DESC);

CREATE TABLE support_messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    sent_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_support_messages_conversation FOREIGN KEY (conversation_id) REFERENCES support_conversations (id) ON DELETE CASCADE,
    CONSTRAINT fk_support_messages_sender FOREIGN KEY (sender_id) REFERENCES users (id),
    CONSTRAINT ck_support_messages_content_not_blank CHECK (btrim(content) <> '')
);

CREATE INDEX ix_support_messages_conversation_sent
    ON support_messages (conversation_id, sent_at ASC);
