ALTER TABLE token_transactions DROP CONSTRAINT IF EXISTS token_transactions_reason_check;
ALTER TABLE token_transactions ADD CONSTRAINT token_transactions_reason_check
    CHECK (reason::text = ANY (ARRAY[
        'DAILY_LOGIN'::character varying,
        'VERIFICATION_BONUS'::character varying,
        'PREMIUM_GRANT'::character varying,
        'SESSION_EXTENSION'::character varying,
        'EXTRA_INVITATION'::character varying,
        'PURCHASED'::character varying,
        'SUPER_LIKE'::character varying
    ]::text[]));
