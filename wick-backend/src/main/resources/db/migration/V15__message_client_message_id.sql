-- Idempotency key for chat messages, used to de-duplicate client retries.
ALTER TABLE messages ADD COLUMN client_message_id VARCHAR(255);
ALTER TABLE messages ADD CONSTRAINT uq_messages_client_message_id UNIQUE (client_message_id);
